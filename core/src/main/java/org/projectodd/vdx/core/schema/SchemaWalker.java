/*
 * Copyright 2016 Red Hat, Inc, and individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.projectodd.vdx.core.schema;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.namespace.QName;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.projectodd.vdx.core.Tree;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

public class SchemaWalker {

    public SchemaWalker(final List<URL> schemas) {
        this.schemaSources.addAll(schemas);
    }

    public Tree<SchemaElement> walk() {
        if (this.walkedSchemas.isEmpty()) {
            this.schemaSources.forEach(this::walk);
        }

        resolveElementReferences(this.tree);
        applyBaseToTypes();
        applyTypesToElement(this.tree);

        return this.tree;
    }

    private void walk(final URL url) {
        if (this.walkedSchemas.containsValue(url)) {
            return;
        }

        final Deque<Tree<SchemaElement>> stack = new ArrayDeque<>();
        stack.push(this.tree);

        final ContentHandler handler = new DefaultHandler() {
            @Override
            public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
                final String nameAttr = attributes.getValue("name");
                switch (qName) {
                    case "xs:schema":
                        final String targetNS = attributes.getValue("targetNamespace");
                        this.namespaceMappings = extractNamespaceMappings(attributes);
                        this.namespaceMappings.put("DEFAULT", targetNS);
                        walkedSchemas.put(targetNS, url);
                        break;

                    case "xs:element":
                        final String refAttr = attributes.getValue("ref");
                        if (refAttr != null) {
                            currentElement = new SchemaElement(qname(refAttr), true);
                        } else {
                            final QName elName = qname(nameAttr);
                            currentElement = new SchemaElement(elName, qname(attributes.getValue("type")));
                            elements.put(elName, currentElement);
                        }

                        activeStack().push(activeStack().peek().addChild(currentElement));
                        break;

                    case "xs:attribute":
                        if (currentType != null) {
                            currentType.addAttribute(nameAttr);
                        } else if (currentElement != null) {
                            currentElement.addAttribute(nameAttr);
                        }
                        break;

                    case "xs:complexType":
                        if (nameAttr != null) {
                            final QName nameQName = qname(nameAttr);
                            currentType = types.get(nameQName);
                            if (currentType == null) {
                                currentType = new ComplexType(nameQName);
                                types.put(nameQName, currentType);
                            }
                            currentTypeElementsStack = new ArrayDeque<>();
                            currentTypeElementsTree = new Tree<>();
                            currentTypeElementsStack.push(currentTypeElementsTree);
                        } else {
                            innerComplexTypeDepth++;
                        }
                        break;

                    case "xs:extension":
                        final QName base = qname(attributes.getValue("base"));
                        if (currentType != null) {
                            currentType.base(base);
                        } else if (currentElement != null) {
                            currentElement.base(base);
                        }
                        break;
                }
            }

            @Override
            public void endElement(String uri, String localName, String qName) throws SAXException {
                switch (qName) {
                    case "xs:element":
                        if (activeStack().peek().value() != null &&
                                currentElement.name().equals(activeStack().peek().value().name())) {
                            activeStack().pop();
                            currentElement = activeStack().peek().value();
                        } else {
                            currentElement = null;
                        }
                        break;

                    case "xs:complexType":
                        if (innerComplexTypeDepth > 0) {
                        innerComplexTypeDepth--;
                        } else if (currentType != null) {
                            currentType.setElements(currentTypeElementsTree);
                            currentTypeElementsStack = null;
                            currentTypeElementsTree = null;
                            currentType = null;
                        }
                        break;
                }
            }

            private QName qname(final String name) {
                if (name == null) return null;

                final String uri;
                final String local;
                if (name.contains(":")) {
                    final String[] parts = name.split(":");
                    uri = this.namespaceMappings.get(parts[0]);
                    local = parts[1];
                } else {
                    uri = this.namespaceMappings.get("DEFAULT");
                    local = name;
                }

                return new QName(uri, local);
            }

            private Deque<Tree<SchemaElement>> activeStack() {
                return currentTypeElementsStack == null ? stack : currentTypeElementsStack;
            }

            private Map<String, String> namespaceMappings = null;
            private SchemaElement currentElement = null;
            private ComplexType currentType = null;
            private Deque<Tree<SchemaElement>> currentTypeElementsStack = null;
            private Tree<SchemaElement> currentTypeElementsTree = null;
            private int innerComplexTypeDepth = 0;
        };


        try (final InputStream in = url.openStream()) {
            final SAXParser parser = SAXParserFactory.newInstance().newSAXParser();

            final XMLReader reader = parser.getXMLReader();
            //reader.setFeature("http://xml.org/sax/features/namespace-prefixes", true);
            reader.setContentHandler(handler);
            reader.parse(new InputSource(in));
        } catch (IOException | ParserConfigurationException | SAXException e) {
            e.printStackTrace();
        }
    }

    private Map<String, String> extractNamespaceMappings(final Attributes attributes) {
        final Map<String, String> namespaceMappings = new HashMap<>();
        for (int i = 0; i < attributes.getLength(); i++) {
            final String attrName = attributes.getQName(i);
            if (attrName.equals("xmlns")) {
                namespaceMappings.put("DEFAULT", attributes.getValue(i));
            } else if (attrName.startsWith("xmlns:")) {
                final String[] parts = attrName.split(":");
                namespaceMappings.put(parts[1], attributes.getValue(i));
            }
        }

        return namespaceMappings;
    }

    private void applyBaseToTypes() {
        // FIXME: this will fail if there are multiple levels of inheritance
        this.types.values().stream()
                .filter(type -> type.base() != null)
                .forEach(type -> {
                    final ComplexType baseType = this.types.get(type.base());
                    if (baseType != null) {
                        baseType.elements().children().forEach(t -> type.elements().addChild(t));
                        baseType.attributes().forEach(type::addAttribute);
                    }
        });
    }

    private void resolveElementReferences(final Tree<SchemaElement> tree) {
        final SchemaElement el = tree.value();
        if (el != null && el.isReference()) {
            final SchemaElement realEl = this.elements.get(el.qname());
            el.delegate(realEl);
            //FIXME: what about child elements? we can't capture that currently
        }

        tree.children().forEach(this::resolveElementReferences);
    }

    private void applyTypesToElement(final Tree<SchemaElement> tree) {
        final SchemaElement el = tree.value();
        if (el != null) {
            if (el.base() != null) {
                applyTypeToElement(this.types.get(el.base()), tree);
            }

            if (el.type() != null) {
                applyTypeToElement(this.types.get(el.type()), tree);
            }
        }

        tree.children().forEach(this::applyTypesToElement);
    }

    private void applyTypeToElement(final ComplexType type, final Tree<SchemaElement> tree) {
        if (type != null &&
                !tree.value().isTypeApplied(type.name())) {
            tree.value().addAppliedType(type.name());
            tree.value().addAttributes(type.attributes());
            type.elements().children().forEach(tree::addChild);
        }
    }

    private final Map<QName, ComplexType> types = new HashMap<>();
    private final Map<QName, SchemaElement> elements = new HashMap<>();
    private final Map<String, URL> walkedSchemas = new HashMap<>();
    private final List<URL> schemaSources = new ArrayList<>();
    private final Tree<SchemaElement> tree = new Tree<>();
}

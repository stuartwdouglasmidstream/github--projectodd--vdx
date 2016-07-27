package org.projectodd.vdx.core;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import javax.xml.namespace.QName;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.apache.ws.commons.schema.XmlSchemaAll;
import org.apache.ws.commons.schema.XmlSchemaAny;
import org.apache.ws.commons.schema.XmlSchemaAnyAttribute;
import org.apache.ws.commons.schema.XmlSchemaChoice;
import org.apache.ws.commons.schema.XmlSchemaCollection;
import org.apache.ws.commons.schema.XmlSchemaElement;
import org.apache.ws.commons.schema.XmlSchemaException;
import org.apache.ws.commons.schema.XmlSchemaSequence;
import org.apache.ws.commons.schema.walker.XmlSchemaAttrInfo;
import org.apache.ws.commons.schema.walker.XmlSchemaTypeInfo;
import org.apache.ws.commons.schema.walker.XmlSchemaVisitor;
import org.apache.ws.commons.schema.walker.XmlSchemaWalker;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

public class ValidationContext {
    public ValidationContext(final URL document, final URL baseUrl, final List<URL> schemas) throws IOException {
        this.document = document;
        try (final BufferedReader reader = new BufferedReader(new InputStreamReader(document.openStream()))) {
            this.lines = reader.lines().collect(Collectors.toList());
        }
        this.schemas.setBaseUri(baseUrl.toString());
        final Set<String> xmlnses = Util.extractXMLNS(this.lines);
        for (URL url : schemas) {
            try {
                if (Util.providesXMLNS(xmlnses, url)) {
                    try (final Reader reader = new InputStreamReader(url.openStream())) {
                        this.schemas.read(reader);
                    }
                }
            } catch (XmlSchemaException e) {
                System.out.println(e.getMessage());
            }
        }
    }

    public int documentLineCount() {
        return this.lines.size();
    }

    public List<String> extractLines(final int start, final int end) {
        final List<String> ret = new ArrayList<>();
        for (int idx = start; idx < end; idx++) {
            ret.add(this.lines.get(idx));
        }

        return ret;
    }

    public ErrorHandler.HandledResult handle(ValidationError error) {
        return error.type().handler().handle(this, error);
    }

    public Set<List<String>> alternateElementsForAttribute(final String attribute) {
        return alternateElements(true, el -> el.attributes().contains(attribute));
    }

    public Set<List<String>> alternateElementsForElement(final QName element) {
        return alternateElements(false, el -> el.qname().equals(element));
    }

    protected Set<List<String>> alternateElements(final boolean includeValue, final Function<SchemaElement, Boolean> pred) {
        return walkSchemas().pathsToValue(includeValue, pred)
                .stream()
                .map(this::docPathWithPrefix)
                .collect(Collectors.toSet());
    }

    /*
    FIXME; this is pretty weak - it finds the first place the first element in the current path occurs, without regard for it
    actually being the same element. A better approach may be to walk down every prefix path, and order the results by
    the length of overlap.
     */
    private List<String> docPathWithPrefix(final List<SchemaElement> path) {
        final List<String> fullPath = new ArrayList<>();
        final List<List<String>> prefixPaths = walkDoc().pathsToValue(e -> e.equals(path.get(0).name()));

        if (!prefixPaths.isEmpty()) {
            fullPath.addAll(prefixPaths.get(0));
        }

        fullPath.addAll(path.stream().map(SchemaElement::name).collect(Collectors.toList()));

        return fullPath;
    }

    @SuppressWarnings("unchecked")
    public Set<String> attributesForElement(final QName elName) {
        return walkSchemas().reduce(new HashSet<>(), (accum, el) -> {
                if (el.qname().equals(elName)) {
                    accum.addAll(el.attributes());
                    Tree.reduceComplete(accum);
                }

                return accum;
            });
    }

    public Position searchForward(final int startLine, final int startCol, final Pattern regex) {
        if (startLine < this.lines.size()) {
            final String line = this.lines.get(startLine);
            final Matcher matcher = regex.matcher(line);
            if (matcher.find(startCol)) {

                // return next line, since we're zero indexed here, but 1 indexed for lines
                return new Position(startLine + 1, matcher.start() + 1);
            } else {

                return searchForward(startLine + 1, 0, regex);
            }
        } else {

            return null;
        }
    }

    private Tree<String> walkDoc() {
        if (this.walkedDoc == null) {
            this.walkedDoc = new Tree<>();
            final Deque<Tree <String>> stack = new ArrayDeque<>();
            stack.push(this.walkedDoc);

            final ContentHandler handler = new DefaultHandler() {
                @Override
                public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
                    final Tree<String> el = new Tree<>(qName);
                    stack.peek().addChild(el);
                    stack.push(el);
                }

                @Override
                public void endElement(String uri, String localName, String qName) throws SAXException {
                    stack.pop();
                }
            };

            try (final InputStream in = this.document.openStream()) {
                final SAXParser parser = SAXParserFactory.newInstance().newSAXParser();

                final XMLReader reader = parser.getXMLReader();
                //reader.setFeature("http://xml.org/sax/features/namespace-prefixes", true);
                reader.setContentHandler(handler);
                reader.parse(new InputSource(in));
            } catch (IOException | ParserConfigurationException | SAXException e) {
                e.printStackTrace();
            }
        }

        return this.walkedDoc;
    }

    private Tree<SchemaElement> walkSchemas() {
        if (this.walkedSchemas == null) {
            this.walkedSchemas = new Tree<>();
            final Deque<Tree <SchemaElement>> stack = new ArrayDeque<>();
            final Map<QName, Tree<SchemaElement>> elementCache = new HashMap<>();

            stack.push(this.walkedSchemas);

            final XmlSchemaWalker walker = new XmlSchemaWalker(this.schemas, new XmlSchemaVisitor() {
                @Override
                public void onEnterElement(XmlSchemaElement element, XmlSchemaTypeInfo typeInfo, boolean previouslyVisited) {
                    final QName qName = element.getQName();
                    Tree<SchemaElement> el = elementCache.get(qName);
                    if (el == null) {
                        el = new Tree<>(new SchemaElement(qName));
                        elementCache.put(qName, el);
                    }
                    stack.peek().addChild(el);
                    stack.push(el);
                }

                @Override
                public void onExitElement(XmlSchemaElement element, XmlSchemaTypeInfo typeInfo, boolean previouslyVisited) {
                    stack.pop();
                }

                @Override
                public void onVisitAttribute(XmlSchemaElement element, XmlSchemaAttrInfo attrInfo) {
                    stack.peek().value().addAttribute(attrInfo.getAttribute().getName());
                }

                @Override
                public void onEndAttributes(XmlSchemaElement element, XmlSchemaTypeInfo typeInfo) {

                }

                @Override
                public void onEnterSubstitutionGroup(XmlSchemaElement base) {

                }

                @Override
                public void onExitSubstitutionGroup(XmlSchemaElement base) {

                }

                @Override
                public void onEnterAllGroup(XmlSchemaAll all) {

                }

                @Override
                public void onExitAllGroup(XmlSchemaAll all) {

                }

                @Override
                public void onEnterChoiceGroup(XmlSchemaChoice choice) {

                }

                @Override
                public void onExitChoiceGroup(XmlSchemaChoice choice) {

                }

                @Override
                public void onEnterSequenceGroup(XmlSchemaSequence seq) {

                }

                @Override
                public void onExitSequenceGroup(XmlSchemaSequence seq) {

                }

                @Override
                public void onVisitAny(XmlSchemaAny any) {

                }

                @Override
                public void onVisitAnyAttribute(XmlSchemaElement element, XmlSchemaAnyAttribute anyAttr) {

                }
            });

            Arrays.stream(this.schemas.getXmlSchemas()).forEach(s -> s.getElements().values().forEach(walker::walk));
        }

        return this.walkedSchemas;
    }

    private final URL document;
    private final List<String> lines;
    private final XmlSchemaCollection schemas = new XmlSchemaCollection();
    private Tree<SchemaElement> walkedSchemas = null;
    private Tree<String> walkedDoc = null;


    public class Position {
        public final int line;
        public final int col;

        public Position(int line, int col) {
            this.line = line;
            this.col = col;
        }
    }
}

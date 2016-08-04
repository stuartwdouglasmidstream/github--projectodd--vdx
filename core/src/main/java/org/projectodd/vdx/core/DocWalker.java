package org.projectodd.vdx.core;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayDeque;
import java.util.Deque;

import javax.xml.namespace.QName;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.InputSource;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

public class DocWalker {

    public DocWalker(final URL document) {
        this.document = document;
    }

    public Tree<DocElement> walk() {
        final Tree<DocElement> tree = new Tree<>();
        final Deque<Tree <DocElement>> stack = new ArrayDeque<>();
        stack.push(tree);

        final ContentHandler handler = new DefaultHandler() {
            @Override
            public void setDocumentLocator(Locator locator) {
                this.locator = locator;
            }

            @Override
            public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
                final String namespace = attributes.getValue("xmlns");
                if (namespace != null) {
                    currentNamespace = namespace;
                }
                stack.push(stack.peek().addChild(new DocElement(qname(qName), attributes)
                                                         .startPosition(lastPosition)));
            }

            @Override
            public void endElement(String uri, String localName, String qName) throws SAXException {
                final Position pos = position();
                stack.peek().value().endPosition(pos);
                storePosition(pos);
                stack.pop();
            }

            @Override
            public void characters(char[] ch, int start, int length) throws SAXException {
                storePosition(position());
            }

            @Override
            public void ignorableWhitespace(char[] ch, int start, int length) throws SAXException {
                storePosition(position());
            }

            private void storePosition(final Position current) {
                if (current.line > lastPosition.line ||
                        (current.line == lastPosition.line &&
                        current.col > lastPosition.col)) {
                    this.lastPosition = current;
                }

            }

            private QName qname(final String local) {
                if (currentNamespace != null) {
                    return new QName(currentNamespace, local);
                } else {
                    return QName.valueOf(local);
                }
            }

            private Position position() {
                return new Position(locator.getLineNumber(), locator.getColumnNumber());
            }

            private Locator locator = null;
            private Position lastPosition = new Position(1, 2);
            private String currentNamespace = null;
        };

        try (final InputStream in = this.document.openStream()) {
            final SAXParser parser = SAXParserFactory.newInstance().newSAXParser();

            final XMLReader reader = parser.getXMLReader();
            reader.setContentHandler(handler);
            reader.parse(new InputSource(in));
        } catch (IOException | ParserConfigurationException | SAXException e) {
            e.printStackTrace();
        }

        return tree;
    }

    private final URL document;
}

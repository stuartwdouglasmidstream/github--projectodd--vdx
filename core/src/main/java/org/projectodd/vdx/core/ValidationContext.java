package org.projectodd.vdx.core;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import javax.xml.namespace.QName;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.projectodd.vdx.core.schema.SchemaElement;
import org.projectodd.vdx.core.schema.SchemaWalker;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

public class ValidationContext {
    public ValidationContext(final URL document, final List<URL> schemas) throws IOException {
        this.document = document;
        try (final BufferedReader reader = new BufferedReader(new InputStreamReader(document.openStream()))) {
            this.lines = reader.lines().collect(Collectors.toList());
        }

        final Set<String> xmlnses = Util.extractXMLNS(this.lines);
        for (URL url : schemas) {
            if (Util.providesXMLNS(xmlnses, url)) {
                this.schemas.add(url);
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

    public List<List<SchemaElement>> alternateElementsForAttribute(final String attribute) {
        return alternateElements(true, el -> el.attributes().contains(attribute));
    }

    public List<List<SchemaElement>> alternateElementsForElement(final QName element) {
        return alternateElements(false, el -> el.qname().equals(element));
    }

    protected List<List<SchemaElement>> alternateElements(final boolean includeValue, final Function<SchemaElement, Boolean> pred) {
        return walkSchemas().pathsToValue(includeValue, pred)
                .stream()
                .map(this::docPathWithPrefix)
                .collect(Collectors.toList());
    }

    /*
    FIXME; this is pretty weak - it finds the first place the first element in the current path occurs, without regard for it
    actually being the same element. A better approach may be to walk down every prefix path, and order the results by
    the length of overlap.
     */
    private List<SchemaElement> docPathWithPrefix(final List<SchemaElement> path) {
        final List<SchemaElement> fullPath = new ArrayList<>();
        final List<List<String>> prefixPaths = walkDoc().pathsToValue(e -> e.equals(path.get(0).name()));

        if (!prefixPaths.isEmpty()) {
            fullPath.addAll(prefixPaths.get(0).stream()
                                    .map(e -> new SchemaElement(QName.valueOf(e)))
                                    .collect(Collectors.toList()));
        }

        fullPath.addAll(path);

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
                    stack.push(stack.peek().addChild(qName));
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
            this.walkedSchemas = new SchemaWalker(this.schemas).walk();
        }

        return this.walkedSchemas;
    }

    private final URL document;
    private final List<String> lines;
    private final List<URL> schemas = new ArrayList<>();
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

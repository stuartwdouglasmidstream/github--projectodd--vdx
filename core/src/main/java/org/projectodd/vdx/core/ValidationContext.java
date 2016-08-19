package org.projectodd.vdx.core;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import javax.xml.namespace.QName;

import org.projectodd.vdx.core.schema.SchemaElement;
import org.projectodd.vdx.core.schema.SchemaPathPrefixFinder;
import org.projectodd.vdx.core.schema.SchemaWalker;

import static java.util.Collections.EMPTY_LIST;

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

    public ValidationContext prefixFinder(final SchemaPathPrefixFinder finder) {
        this.prefixFinder = finder;

        return this;
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

    private List<List<SchemaElement>> alternateElements(final boolean includeValue, final Function<SchemaElement, Boolean> pred) {
        return schemaTree().pathsToValue(includeValue, pred)
                .stream()
                .map(this::schemaPathWithPrefix)
                .collect(Collectors.toList());
    }

    private List<SchemaElement> schemaPathWithPrefix(final List<SchemaElement> path) {
        if (this.prefixFinder == null) {
            this.prefixFinder = p -> {
                final List<List<DocElement>> prefixPaths = documentTree().pathsToValue(e -> e.name().equals(p.get(0).getLocalPart()));

                if (!prefixPaths.isEmpty()) {
                    return prefixPaths.get(0)
                            .stream()
                            .map(e -> QName.valueOf(e.name()))
                            .collect(Collectors.toList());
                }

                return EMPTY_LIST;
            };
        }

        final List<QName> prefix = this.prefixFinder.prefixFor(path.stream()
                                                                       .map(SchemaElement::qname)
                                                                       .collect(Collectors.toList()));
        if (prefix != null && !prefix.isEmpty()) {
            final List<SchemaElement> fullPath = new ArrayList<>();
            fullPath.addAll(prefix.stream()
                                    .map(SchemaElement::new)
                                    .collect(Collectors.toList()));
            fullPath.addAll(path);

            return fullPath;
        }

        return path;
    }

    @SuppressWarnings("unchecked")
    public Set<String> attributesForElement(final QName elName) {
        return schemaTree().reduce(new HashSet<>(), (accum, el) -> {
                if (el.qname().equals(elName)) {
                    accum.addAll(el.attributes());
                    Tree.reduceComplete(accum);
                }

                return accum;
            });
    }

    public Position searchForward(final int startLine, final int startCol, final Pattern regex) {
        int loopStartLine = startLine;
        int loopStartCol = startCol;
        while (loopStartLine < this.lines.size()) {
            final String line = this.lines.get(loopStartLine);
            final Matcher matcher = regex.matcher(line);

            if (loopStartCol < line.length() &&
                    matcher.find(loopStartCol)) {

                // return next line, since we're zero indexed here, but 1 indexed for lines
                return new Position(loopStartLine + 1, matcher.start() + 1);
            } else {
                loopStartLine++;
                loopStartCol = 0;
            }
        }

        return null;
    }

    public Position searchBackward(final int startLine, final int startCol, final Pattern regex) {
        int loopStartLine = startLine;
        int loopStartCol = startCol;
        while (loopStartLine >= 0) {
            final String line = this.lines.get(loopStartLine);
            final Matcher matcher = regex.matcher(line);
            if (loopStartCol >= line.length()) {
                loopStartCol = line.length() - 1;
            }

            if (loopStartCol >= 0 &&
                    matcher.find(loopStartCol)) {

                // return next line, since we're zero indexed here, but 1 indexed for lines
                return new Position(loopStartLine + 1, matcher.start() + 1);
            } else if (loopStartCol > 0) {
                loopStartCol--;
            } else {
                loopStartLine--;
                loopStartCol = Integer.MAX_VALUE;
            }
        }

        return null;
    }

    public List<List<DocElement>> pathsToDocElement(final Function<DocElement, Boolean> pred) {
        return documentTree().pathsToValue(true, pred);
    }


    public List<DocElement> pathToDocElement(final Function<DocElement, Boolean> pred) {
        List<List<DocElement>> paths = pathsToDocElement(pred);
        if (!paths.isEmpty()) {

            return paths.get(0);
        }

        return null;
    }

    public List<List<DocElement>> docElementSiblings(final List<DocElement> element, final Function<DocElement, Boolean> pred) {
        final List<DocElement> parentPath = element.subList(0, element.size() - 1);

        return pathsToDocElement(pred).stream()
                .filter(p -> !p.equals(element))
                .filter(p -> p.subList(0, p.size() - 1).equals(parentPath))
                .collect(Collectors.toList());
    }

    private Tree<DocElement> documentTree() {
        if (this.walkedDoc == null) {
            this.walkedDoc = new DocWalker(this.document).walk();
        }

        return this.walkedDoc;
    }

    private Tree<SchemaElement> schemaTree() {


        if (this.walkedSchemas == null) {
            this.walkedSchemas = new SchemaWalker(this.schemas).walk();
        }

        return this.walkedSchemas;
    }

    private final URL document;
    private final List<String> lines;
    private final List<URL> schemas = new ArrayList<>();
    private Tree<SchemaElement> walkedSchemas = null;
    private Tree<DocElement> walkedDoc = null;
    private SchemaPathPrefixFinder prefixFinder = null;
}

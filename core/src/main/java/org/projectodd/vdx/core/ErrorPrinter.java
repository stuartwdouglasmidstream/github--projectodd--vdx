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

package org.projectodd.vdx.core;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.projectodd.vdx.core.schema.SchemaPathGate;
import org.projectodd.vdx.core.schema.SchemaPathPrefixProvider;

public class ErrorPrinter {
    public ErrorPrinter(final URL document, final List<URL> schemas) throws IOException {
        this.context = new ValidationContext(document, schemas);
        this.docURL = document;
    }

    public void print(ValidationError error) {
        final ErrorHandler.HandledResult res = this.context.handle(error);

        if (res != null) {
            final String[] docPathParts = docURL.getPath().split("/");

            final StringBuilder out = new StringBuilder()
                    .append('\n')
                    .append(divider(I18N.validationErrorIn(docPathParts[docPathParts.length - 1])));

            formatResult(out, res);

            out.append(divider(1))
                    .append('\n');

            this.printer.println(Util.withPrefixAfterNth(2, "|", out.toString()));
        }
    }

    public ErrorPrinter printer(final Printer printer) {
        if (printer == null) {
            throw new IllegalArgumentException("printer can't be null");
        }
        this.printer = printer;

        return this;
    }

    public ErrorPrinter stringifiers(final List<Stringifier> stringifiers) {
        if (stringifiers != null) {
            stringifiers.forEach(Stringify::registerStringifier);
        }

        return this;
    }

    public ErrorPrinter pathGate(final SchemaPathGate gate) {
        this.context.pathGate(gate);

        return this;
    }

    public ErrorPrinter prefixProvider(final SchemaPathPrefixProvider prefixProvider) {
        this.context.prefixProvider(prefixProvider);

        return this;
    }

    private void formatResult(final StringBuilder out, final ErrorHandler.HandledResult result) {
        final int linum = result.line();
        final int maxLinumWidth = ("" + linum + CONTEXT_LINES).length();
        final List<PrefixedLine> preambleLines = preambleLines(linum, maxLinumWidth);
        final List<PrefixedLine> postambleLines = postambleLines(linum, maxLinumWidth);
        final List<PrefixedLine> allLines = new ArrayList<>();
        allLines.addAll(preambleLines);
        allLines.addAll(postambleLines);
        final int removeSpaces = smallestPrefixWhitespace(allLines);


        out.append('\n')
                .append(Util.withPrefix(" ", ambleString(preambleLines, removeSpaces)))
                .append(alignPointerMessage(maxLinumWidth + result.column() + 2 - removeSpaces, result.primaryMessages()))
                .append("\n")
                .append(Util.withPrefix(" ", ambleString(postambleLines, removeSpaces)));

        if (!result.secondaryMessages().isEmpty()) {
            result.secondaryMessages().forEach(m -> out.append("\n").append(Util.withPrefix(" ", m.toString())).append("\n"));
        }

        if (!result.secondaryResults().isEmpty()) {
            result.secondaryResults().forEach(r -> formatResult(out, r));
        } else {
            out.append("\n");
        }

        if (result.originalMessage() != null) {
            out.append(Util.withPrefix(" ", I18N.lookup(I18N.Key.ORIGINAL_ERROR))).append("\n")
                    .append(Util.withPrefix(" > ", Util.indentLinesAfterNth(2, WRAPPED_LINE_INDENT, Util.wrapString(WRAPPED_LINE_WIDTH,
                                                                                                                    result.originalMessage()))))
                    .append("\n\n");
        }
    }

    private static final int CONTEXT_LINES = 3;

    private String linumPrefix(final int linum, final int maxWidth) {
        return String.format("%" + maxWidth + "s: ", linum + 1);
    }

    private List<PrefixedLine> extractLines(final int maxLinumWidth, final int start, final int end) {
        final List<PrefixedLine> ret = new ArrayList<>();
        int linum = start;
        for (String line: this.context.extractLines(start, end)) {
            ret.add(new PrefixedLine(linumPrefix(linum, maxLinumWidth), line));
            linum++;
        }

        return ret;
    }

    private List<PrefixedLine> preambleLines(final int linum, final int maxLinumWidth) {
        return extractLines(maxLinumWidth,
                            CONTEXT_LINES > linum ?  0 : linum - CONTEXT_LINES,
                            linum);
    }

    private List<PrefixedLine> postambleLines(final int linum, final int maxLinumWidth) {
        return extractLines(maxLinumWidth,
                            linum,
                            CONTEXT_LINES + linum > this.context.documentLineCount() ?
                                    this.context.documentLineCount() : linum + CONTEXT_LINES);
    }

    private final Pattern LEADING_WHITESPACE_RE = Pattern.compile("^([ ]+)");

    private int smallestPrefixWhitespace(final List<PrefixedLine> lines) {
        int size = Integer.MAX_VALUE;

        for(PrefixedLine line : lines) {
            final Matcher m = LEADING_WHITESPACE_RE.matcher(line.line);
            if (m.find()) {
                final int len = m.group(1).length();
                size = len < size ? len : size;
            } else {
                size = 0;
            }
        }

        return size;
    }

    private String ambleString(final List<PrefixedLine> lines, final int removePrefixChars) {
        final StringBuilder sb = new StringBuilder();

        lines.forEach(l -> sb.append(l.asString(removePrefixChars)).append('\n'));

        return sb.toString();
    }

    private String alignPointerMessage(final int length, final List<Message> msg) {
        if (msg.isEmpty()) {

            return String.format("%" + (length + POINTER.length()) + "s\n", POINTER);
        }

        // join all the messages together into one string, then split back out. This will handle individual messages that
        // contain \n
        final String[] lines = String.join("\n", msg.stream()
                .map(Object::toString)
                .map(line -> Util.wrapString(WRAPPED_LINE_WIDTH, line))
                .map(line -> Util.indentLinesAfterFirst(WRAPPED_LINE_INDENT, line))
                .collect(Collectors.toList()))
                .split("\n");
        final StringBuilder sb = new StringBuilder();
        sb.append(String.format("%" + (length + lines[0].length() + POINTER.length() + 1) + "s", POINTER + " " + lines[0]))
                .append('\n');
        for (int i = 1; i < lines.length; i++) {
            sb.append(String.format("%" + (length + lines[i].length() + POINTER.length() + 1) + "s", lines[i]))
                    .append('\n');
        }

        return sb.toString();
    }

    private String divider(final int shorten) {
        final StringBuilder ret = new StringBuilder();
        for(int i = 0; i < DIVIDER_WIDTH - shorten; i++) {
            ret.append(DASH);
        }

        return ret.toString();
    }

    private String divider(String heading) {
        final StringBuilder ret = new StringBuilder();
        if (heading != null) {
            ret.append(heading).append(' ');
        }
        ret.append(divider(heading != null ? heading.length() + 1 : 0));

        return ret.append('\n').toString();
    }

    private static final String POINTER = "^^^^";
    private static final char DASH = '-';
    private static final int WRAPPED_LINE_WIDTH = 70;
    private static final int WRAPPED_LINE_INDENT = 2;
    private static final int DIVIDER_WIDTH = 80;

    private final URL docURL;
    private final ValidationContext context;
    private Printer printer = Printer.DEFAULT_PRINTER;

    private class PrefixedLine {
        public final String prefix;
        public final String line;

        PrefixedLine(final String prefix, final String line) {
            this.prefix = prefix;
            this.line = line.replaceAll("\t", "  ");
        }

        String asString(final int removePrefixChars) {
            return String.format("%s%s", prefix, line.substring(removePrefixChars));
        }
    }
}

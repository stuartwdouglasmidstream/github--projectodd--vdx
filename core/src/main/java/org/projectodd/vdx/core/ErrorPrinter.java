package org.projectodd.vdx.core;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ErrorPrinter {
    public ErrorPrinter(final URL document, final List<URL> schemas) throws IOException {
        this(document, schemas, Printer.DEFAULT_PRINTER, null);
    }

    public ErrorPrinter(final URL document, final List<URL> schemas, final Printer printer, final List<Stringifier> stringifiers) throws IOException {
        this.context = new ValidationContext(document, schemas);
        this.docURL = document;
        this.printer = printer;
        if (stringifiers != null) {
            stringifiers.forEach(Stringify::registerStringifier);
        }
    }

    public void print(ValidationError error) {
        final ErrorHandler.HandledResult res = this.context.handle(error);

        if (res != null) {
            final String[] docPathParts = docURL.getPath().split(File.separator);

            final StringBuilder out = new StringBuilder()
                    .append('\n')
                    .append(divider(I18N.validationErrorIn(docPathParts[docPathParts.length - 1])));

            formatResult(out, res);

            out.append(divider())
                    .append('\n');

            this.printer.println(out.toString());
        }
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
                .append(ambleString(preambleLines, removeSpaces))
                .append('\n')
                .append(leftPad(maxLinumWidth + result.column() + 1 - removeSpaces, "^ " +
                        (result.message() != null ? result.message().toString() : "")))
                .append("\n\n")
                .append(ambleString(postambleLines, removeSpaces))
                .append('\n');

        if (result.extraMessage() != null) {
            out.append(result.extraMessage().toString())
                    .append("\n");
        }

        if (result.extraResult() != null) {
            formatResult(out, result.extraResult());
        } else {
            out.append("\n");
        }

        if (result.originalMessage() != null) {
            out.append(I18N.lookup(I18N.Key.ORIGINAL_ERROR)).append("\n")
                    .append(Util.withPrefix("> ", result.originalMessage())).append("\n\n");
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

    private int smallestPrefixWhitespace(final List<PrefixedLine> lines) {
        int size = Integer.MAX_VALUE;

        for(PrefixedLine line : lines) {
            final Matcher m = Pattern.compile("^([ ]+)").matcher(line.line);
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

    private String leftPad(final int length, final String str) {
        return String.format("%" + (length + str.length()) + "s", str);
    }

    private static final char DASH = '=';

    private String divider() {
        return divider(null);
    }

    private String divider(String heading) {
        final int dashes = heading != null ? 80 - heading.length() - 1 : 80;
        final StringBuilder ret = new StringBuilder();
        if (heading != null) {
            ret.append(heading).append(' ');
        }

        for(int i = 0; i < dashes; i++) {
            ret.append(DASH);
        }

        return ret.append('\n').toString();
    }

    private final URL docURL;
    private final ValidationContext context;
    private final Printer printer;

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

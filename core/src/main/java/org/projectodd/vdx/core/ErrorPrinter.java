package org.projectodd.vdx.core;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.List;

public class ErrorPrinter {
    public ErrorPrinter(final URL document, final URL baseUrl, final List<URL> schemas) throws IOException {
        this(document, baseUrl, schemas, Printer.DEFAULT_PRINTER, null);
    }

    public ErrorPrinter(final URL document, final URL baseUrl, final List<URL> schemas, final Printer printer, final List<Stringifier> stringifiers) throws IOException {
        this.context = new ValidationContext(document, baseUrl, schemas);
        this.docURL = document;
        this.printer = printer;
        if (stringifiers != null) {
            stringifiers.forEach(Stringify::registerStringifier);
        }
    }

    public void print(ValidationError error) {
        final ErrorHandler.HandledResult res = this.context.handle(error);

        if (res != null) {
            final int linum = res.line;
            final int maxLinumWidth = ("" + linum + CONTEXT_LINES).length();
            final String[] docPathParts = docURL.getPath().split(File.separator);

            final StringBuilder out = new StringBuilder()
                    .append('\n')
                    .append(divider(I18N.format(I18N.Key.VALIDATION_ERROR_IN, docPathParts[docPathParts.length - 1])))
                    .append('\n')
                    .append(prefixLines(linum, maxLinumWidth))
                    .append('\n')
                    .append(leftPad(maxLinumWidth + res.column + 1, "^ " + res.message.toString()))
                    .append("\n\n")
                    .append(postfixLines(linum, maxLinumWidth))
                    .append('\n');

            if (res.extraMessage != null) {
                out.append(res.extraMessage.toString())
                        .append("\n\n");
            }

            out.append(divider())
                    .append('\n');

            this.printer.println(out.toString());
        }
    }

    private static final int CONTEXT_LINES = 3;

    private String linumPrefix(final int linum, final int maxWidth) {
        return String.format("%" + maxWidth + "s: ", linum + 1);
    }

    private String extractLines(final int maxLinumWidth, final int start, final int end) {
        final StringBuilder ret = new StringBuilder();
        int linum = start;
        for (String line: this.context.extractLines(start, end)) {
            ret.append(linumPrefix(linum, maxLinumWidth))
                    .append(line)
                    .append('\n');
            linum++;
        }

        return ret.toString();
    }

    private String prefixLines(final int linum, final int maxLinumWidth) {
        return extractLines(maxLinumWidth,
                            CONTEXT_LINES > linum ?  0 : linum - CONTEXT_LINES,
                            linum);
    }

    private String postfixLines(final int linum, final int maxLinumWidth) {
        return extractLines(maxLinumWidth,
                            linum,
                            CONTEXT_LINES + linum > this.context.documentLineCount() ?  this.context.documentLineCount() : linum + CONTEXT_LINES);
    }


    private String leftPad(final int length, final String str) {
        return String.format("%" + (length + str.length()) + "s", str);
    }

    private static final char DASH = '=';

    private String divider() {
        return divider(null);
    }

    private String divider(String heading) {
        final int dashes = heading != null ? (80 - heading.length() - 2) / 2 : 40;
        final StringBuilder ret = new StringBuilder();
        for(int i = 0; i < dashes; i++) {
            ret.append(DASH);
        }

        if (heading != null) {
            ret.append(' ')
                    .append(heading)
                    .append(' ');
        }

        for(int i = 0; i < dashes; i++) {
            ret.append(DASH);
        }

        if (ret.length() < 80) {
            ret.append(DASH);
        }

        return ret.append('\n').toString();
    }

    private final URL docURL;
    private final ValidationContext context;
    private final Printer printer;
}

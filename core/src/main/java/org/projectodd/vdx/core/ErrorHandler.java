package org.projectodd.vdx.core;

public interface ErrorHandler {
    HandledResult handle(ValidationContext ctx, ValidationError err);

    class HandledResult {
        public final int line;
        public final int column;
        public final String message;
        public final String extraMessage;

        public HandledResult(final int line, final int column, final String message, final String extraMessage) {
            this.line = line;
            this.column = column;
            this.message = message;
            this.extraMessage = extraMessage;
        }

        public String toString() {
            return "[line=" + line + ", column=" + column +
                    ", message='" + message + "', extraMessage='" +
                    extraMessage + "']";
        }
    }

}

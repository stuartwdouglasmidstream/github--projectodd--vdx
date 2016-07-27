package org.projectodd.vdx.core;

public interface ErrorHandler {
    HandledResult handle(ValidationContext ctx, ValidationError err);

    class HandledResult {
        public final int line;
        public final int column;
        public final Message message;
        public final Message extraMessage;

        public HandledResult(final int line, final int column, final Message message, final Message extraMessage) {
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

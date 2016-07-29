package org.projectodd.vdx.core;

import javax.xml.stream.Location;

public interface ErrorHandler {
    HandledResult handle(ValidationContext ctx, ValidationError err);

    class HandledResult {
        private HandledResult(final int line, final int column, final String originalMessage) {
            this.line = line;
            this.column = column;
            this.originalMessage = originalMessage;
        }

        public static HandledResult from(final ValidationError err) {
            final Location loc = err.location();

            return new HandledResult(loc.getLineNumber(), loc.getColumnNumber(), err.message());
        }

        public HandledResult line(int line) {
            this.line = line;
            return this;
        }

        public HandledResult column(int column) {
            this.column = column;
            return this;
        }

        public HandledResult message(Message message) {
            this.message = message;
            return this;
        }

        public HandledResult message(final I18N.Key key, Object... args) {
            return message(new Message(key, args));
        }

        public HandledResult extraMessage(Message extraMessage) {
            this.extraMessage = extraMessage;
            return this;
        }

        public HandledResult extraMessage(final I18N.Key key, Object... args) {
            return extraMessage(new Message(key, args));
        }

        public int line() {
            return line;
        }

        public int column() {
            return column;
        }

        public String originalMessage() {
            return originalMessage;
        }

        public Message message() {
            return message;
        }

        public Message extraMessage() {
            return extraMessage;
        }

        public String toString() {
            return "[line=" + line + ", column=" + column + ", originalMessage='" +
                    originalMessage + "', message='" + message + "', extraMessage='" +
                    extraMessage + "']";
        }

        private int line;
        private int column;
        private final String originalMessage;
        private Message message;
        private Message extraMessage;
    }

}

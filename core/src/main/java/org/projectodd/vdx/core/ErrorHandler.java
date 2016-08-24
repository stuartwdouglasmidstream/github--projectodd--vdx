package org.projectodd.vdx.core;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.xml.stream.Location;

public interface ErrorHandler {
    HandledResult handle(ValidationContext ctx, ValidationError err);

    class HandledResult {
        public HandledResult(final int line, final int column, final String originalMessage) {
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

        public HandledResult primaryMessage(Message message) {
            if (message != null) {
                this.primaryMessages.add(message);
            }

            return this;
        }

        public HandledResult primaryMessage(final I18N.Key key, Object... args) {
            return primaryMessage(new Message(key, args));
        }

        public List<Message> primaryMessages() {
            return Collections.unmodifiableList(primaryMessages);
        }

        public HandledResult secondaryMessage(Message extraMessage) {
            if (extraMessage != null) {
                this.secondaryMessages.add(extraMessage);
            }

            return this;
        }

        public HandledResult secondaryMessage(final I18N.Key key, Object... args) {
            return secondaryMessage(new Message(key, args));
        }

        public List<Message> secondaryMessages() {
            return Collections.unmodifiableList(secondaryMessages);
        }

        public List<HandledResult> secondaryResults() {
            return Collections.unmodifiableList(secondaryResults);
        }

        public HandledResult secondaryResult(final HandledResult extraResult) {
            if (extraResult != null) {
                this.secondaryResults.add(extraResult);
            }

            return this;
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


        public String toString() {
            return "[line=" + line + ", column=" + column + ", originalMessage='" +
                    originalMessage + "', primaryMessages=" + primaryMessages + ", secondaryMessages=" +
                    secondaryMessages + "]";
        }

        private int line;
        private int column;
        private final String originalMessage;
        private List<Message> primaryMessages = new ArrayList<>();
        private List<Message> secondaryMessages = new ArrayList<>();
        private List<HandledResult> secondaryResults = new ArrayList<>();
    }

}

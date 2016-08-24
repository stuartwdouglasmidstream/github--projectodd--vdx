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

        public HandledResult message(Message message) {
            if (message != null) {
                this.messages.add(message);
            }

            return this;
        }

        public HandledResult message(final I18N.Key key, Object... args) {
            return message(new Message(key, args));
        }

        public HandledResult extraMessage(Message extraMessage) {
            if (extraMessage != null) {
                this.extraMessages.add(extraMessage);
            }

            return this;
        }

        public HandledResult extraMessage(final I18N.Key key, Object... args) {
            return extraMessage(new Message(key, args));
        }

        public List<Message> extraMessages() {
            return Collections.unmodifiableList(extraMessages);
        }

        public List<HandledResult> extraResults() {
            return Collections.unmodifiableList(extraResults);
        }

        public HandledResult extraResult(final HandledResult extraResult) {
            if (extraResult != null) {
                this.extraResults.add(extraResult);
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

        public List<Message> messages() {
            return Collections.unmodifiableList(messages);
        }


        public String toString() {
            return "[line=" + line + ", column=" + column + ", originalMessage='" +
                    originalMessage + "', messages=" + messages + ", extraMessages=" +
                    extraMessages + "]";
        }

        private int line;
        private int column;
        private final String originalMessage;
        private List<Message> messages = new ArrayList<>();
        private List<Message> extraMessages = new ArrayList<>();
        private List<HandledResult> extraResults = new ArrayList<>();
    }

}

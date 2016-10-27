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

        public HandledResult addPrimaryMessage(Message message) {
            if (message != null) {
                this.primaryMessages.add(message);
            }

            return this;
        }

        public HandledResult addPrimaryMessage(final I18N.Key key, Object... args) {
            return addPrimaryMessage(new Message(key, args));
        }

        public List<Message> primaryMessages() {
            return Collections.unmodifiableList(primaryMessages);
        }

        public HandledResult addSecondaryMessage(Message extraMessage) {
            if (extraMessage != null) {
                this.secondaryMessages.add(extraMessage);
            }

            return this;
        }

        public HandledResult addSecondaryMessage(final I18N.Key key, Object... args) {
            return addSecondaryMessage(new Message(key, args));
        }

        public List<Message> secondaryMessages() {
            return Collections.unmodifiableList(secondaryMessages);
        }

        public List<HandledResult> secondaryResults() {
            return Collections.unmodifiableList(secondaryResults);
        }

        public HandledResult addSecondaryResult(final HandledResult extraResult) {
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

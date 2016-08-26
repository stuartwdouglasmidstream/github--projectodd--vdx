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

public interface Stringifier {
    Stringifier DEFAULT_STRINGIFIER = new Stringifier() {
        @Override
        public Class handledClass() {
            return null;
        }

        @Override
        public String asString(Object value) {
            return value.toString();
        }
    };


    @SuppressWarnings("unchecked")
    default boolean handles(final Object value) {
        return handledClass().isAssignableFrom(value.getClass());
    }

    Class handledClass();

    String asString(Object value);
}

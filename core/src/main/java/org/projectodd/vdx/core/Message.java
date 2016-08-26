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

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class Message {
    public Message(final I18N.Key template, final Object... values) {
        this.template = template;
        this.values = Arrays.asList(values);
    }

    public List<Object> rawValues() {
        return Collections.unmodifiableList(this.values);
    }

    public List<String> stringValues() {
        return this.values.stream()
                .map(Stringify::asString)
                .collect(Collectors.toList());
    }

    public I18N.Key template() {
        return this.template;
    }

    @Override
    public String toString() {
        return I18N.format(this.template, stringValues().toArray());
    }

    private final I18N.Key template;
    private final List<Object> values;
}

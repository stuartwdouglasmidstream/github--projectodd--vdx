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

import java.util.List;
import java.util.stream.Collectors;

import org.projectodd.vdx.core.schema.SchemaElement;

public class NonElementListStringifier implements Stringifier {

    public NonElementListStringifier(final int listThreshold, final int limit) {
        this(listThreshold, limit, false);
    }

    NonElementListStringifier(final int threshold, final int limit, final boolean asBulletList) {
        this.listThreshold = threshold;
        this.limit = limit;
        this.asBulletList = asBulletList;
    }

    @Override
    @SuppressWarnings("unchecked")
    public boolean handles(Object value) {
        return Stringifier.super.handles(value) &&
                !SchemaElement.class.isAssignableFrom(((List)value).get(0).getClass());
    }

    @Override
    public Class handledClass() {
        return List.class;
    }

    @Override
    public String asString(Object value) {
        final List<?> list = (List<?>)value;

        final List<String> values = list.stream()
                .map(Stringify::asString)
                .distinct()
                .limit(limit > 0 ? limit : list.size())
                .collect(Collectors.toList());

        final StringBuilder sb = new StringBuilder();
        if (this.listThreshold == -1 ||
                values.size() <= this.listThreshold) {
            sb.append(Util.asCommaString(values)).append(' ');
        } else {
            sb.append('\n');
            if (this.asBulletList) {
                values.forEach(v -> sb.append("- ").append(v).append('\n'));
            } else {
                sb.append(Util.asColumns(values));
            }
        }

        if (this.limit < list.size()) {
            sb.append(I18N.format(I18N.Key.AND_N_MORE, list.size() - limit));
        }

        return sb.toString();
    }


    private final int listThreshold;
    private final int limit;
    private final boolean asBulletList;
}


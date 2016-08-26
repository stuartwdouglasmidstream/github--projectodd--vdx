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

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.xml.namespace.QName;

import org.xml.sax.Attributes;

public class DocElement {
    public DocElement(final QName name, final Attributes attributes) {
        this(name, attributesToMap(attributes));
    }

    public DocElement(final QName name, final Map<String, String> attributes) {
        this.name = name;
        if (attributes != null) {
            this.attributes.putAll(attributes);
        }
    }

    public Position startPosition() {
        return startPosition;
    }

    public DocElement startPosition(Position position) {
        if (position != null) {
            this.startPosition = position;
        }
        return this;
    }

    public Position endPosition() {
        return endPosition;
    }

    public DocElement endPosition(Position endPosition) {
        this.endPosition = endPosition;
        return this;
    }

    public boolean encloses(final Position pos) {
        return this.startPosition.compareTo(pos) <= 0 &&
                this.endPosition.compareTo(pos) >= 0;
    }

    public String name() {
        return name.getLocalPart();
    }

    public QName qname() {
        return name;
    }

    public Map<String, String> attributes() {
        return Collections.unmodifiableMap(attributes);
    }

    public static Map<String, String> attributesToMap(final Attributes attributes) {
        final Map<String, String> attrMap = new HashMap<>();
        for (int i = 0; i < attributes.getLength(); i++) {
            attrMap.put(attributes.getQName(i), attributes.getValue(i));
        }

        return attrMap;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) return false;
        if (obj == this) return true;
        if (!(obj instanceof DocElement)) return false;

        final DocElement that = (DocElement)obj;

        return this.qname().equals(that.qname()) &&
                this.attributes.equals(that.attributes) &&
                this.startPosition.equals(that.startPosition) &&
                this.endPosition.equals(that.endPosition);
    }

    @Override
    public String toString() {
        return "<name=" + name + ", attributes=" + attributes + ">";
    }

    private Position startPosition = new Position(-1, -1);
    private Position endPosition = new Position(-1, -1);
    private final QName name;
    private final Map<String, String> attributes = new HashMap<>();
}

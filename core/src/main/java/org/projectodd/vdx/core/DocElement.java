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

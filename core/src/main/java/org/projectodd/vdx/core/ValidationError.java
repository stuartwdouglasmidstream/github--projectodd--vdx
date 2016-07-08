package org.projectodd.vdx.core;

import java.util.Set;

import javax.xml.namespace.QName;
import javax.xml.stream.Location;

public class ValidationError {
    public ValidationError(final ErrorType type, final Location location, final QName element) {
        this(type, location, element, null, null, null);
    }

    public ValidationError(final ErrorType type, final Location location, final QName element,
                           final Set<String> alternatives) {
        this(type, location, element, null, null, alternatives);
    }

    public ValidationError(final ErrorType type, final Location location, final QName element,
                           final QName attribute, final Set<String> alternatives) {
        this(type, location, element, attribute, null, alternatives);
    }

    public ValidationError(final ErrorType type, final Location location, final QName element,
                           final QName attribute, final String attributeValue,
                           final Set<String> alternatives) {
        this.type = type;
        this.location = location;
        this.element = element;
        this.attribute = attribute;
        this.attributeValue = attributeValue;
        this.alternatives = alternatives;
    }

    public ErrorType type() {
        return type;
    }

    public Location location() {
        return location;
    }

    public QName element() {
        return element;
    }

    public QName attribute() {
        return attribute;
    }

    public String attributeValue() {
        return attributeValue;
    }

    public Set<String> alternatives() {
        return alternatives;
    }

    private final ErrorType type;
    private final Location location;
    private final QName element;
    private final QName attribute;
    private final String attributeValue;
    private final Set<String> alternatives;

}

package org.projectodd.vdx.core;

import java.util.Collections;
import java.util.Set;

import javax.xml.namespace.QName;
import javax.xml.stream.Location;
import javax.xml.stream.XMLStreamException;

public class ValidationError {
    public ValidationError(final ErrorType type, final String message, final Location location) {
        this.type = type;
        this.location = location;
        this.message = message;
    }

    public static ValidationError from(final XMLStreamException error, final ErrorType type) {
        return new ValidationError(type, error.getMessage(), error.getLocation());
    }

    public ErrorType type() {
        return type;
    }
    
    public Location location() {
        return location;
    }

    public String message() {
        return message;
    }

    public String fallbackMessage() {
        return fallbackMessage;
    }

    public ValidationError fallbackMessage(String fallbackMessage) {
        this.fallbackMessage = fallbackMessage;
        return this;
    }

    public QName element() {
        return element;
    }

    public ValidationError element(final QName element) {
        this.element = element;

        return this;
    }
    
    public QName attribute() {
        return attribute;
    }

    public ValidationError attribute(final QName attribute) {
        this.attribute = attribute;

        return this;
    }

    public String attributeValue() {
        return attributeValue;
    }

    public ValidationError attributeValue(final String attributeValue) {
        this.attributeValue = attributeValue;

        return this;
    }

    public Set<String> alternatives() {
        return alternatives;
    }

    public ValidationError alternatives(final Set<String> alternatives) {
        this.alternatives = Collections.unmodifiableSet(alternatives);

        return this;
    }

    private final ErrorType type;
    private final Location location;
    private final String message;
    private String fallbackMessage = null;
    private QName element = null;
    private QName attribute = null;
    private String attributeValue = null;
    private Set<String> alternatives = null;

}

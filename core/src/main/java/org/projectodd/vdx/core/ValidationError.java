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

import java.util.HashSet;
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

    public static ValidationError from(final ValidationError error, final ErrorType type) {
        return new ValidationError(type, error.message(), error.location());
    }

    public ErrorType type() {
        return type;
    }
    
    public Location location() {
        return location;
    }

    public Position position() {
        return new Position(this.location.getLineNumber(), this.location.getColumnNumber());
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
        this.alternatives.addAll(alternatives);

        return this;
    }

    private final ErrorType type;
    private final Location location;
    private final String message;
    private String fallbackMessage = null;
    private QName element = null;
    private QName attribute = null;
    private String attributeValue = null;
    private final Set<String> alternatives = new HashSet<>();

}

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

import org.projectodd.vdx.core.handlers.DuplicateAttributeHandler;
import org.projectodd.vdx.core.handlers.DuplicateElementHandler;
import org.projectodd.vdx.core.handlers.InvalidAttributeValueHandler;
import org.projectodd.vdx.core.handlers.RequiredAttributeMissingHandler;
import org.projectodd.vdx.core.handlers.RequiredElementMissingHandler;
import org.projectodd.vdx.core.handlers.RequiredElementsMissingHandler;
import org.projectodd.vdx.core.handlers.UnexpectedAttributeHandler;
import org.projectodd.vdx.core.handlers.UnexpectedElementHandler;
import org.projectodd.vdx.core.handlers.UnknownErrorHandler;
import org.projectodd.vdx.core.handlers.UnsupportedElementHandler;

public enum ErrorType {
    DUPLICATE_ATTRIBUTE(DuplicateAttributeHandler.class),
    DUPLICATE_ELEMENT(DuplicateElementHandler.class),
    INVALID_ATTRIBUTE_VALUE(InvalidAttributeValueHandler.class),
    REQUIRED_ATTRIBUTE_MISSING(RequiredAttributeMissingHandler.class),
    REQUIRED_ELEMENT_MISSING(RequiredElementMissingHandler.class),
    REQUIRED_ELEMENTS_MISSING(RequiredElementsMissingHandler.class),
    UNEXPECTED_ATTRIBUTE(UnexpectedAttributeHandler.class),
    UNEXPECTED_ELEMENT(UnexpectedElementHandler.class),
    UNKNOWN_ERROR(UnknownErrorHandler.class),
    UNSUPPORTED_ELEMENT(UnsupportedElementHandler.class);

    private final Class<? extends ErrorHandler> handlerClass;

    public ErrorHandler handler() {
        try {
            return this.handlerClass.newInstance();
        } catch (IllegalAccessException | InstantiationException e) {
            throw new RuntimeException(e);
        }
    }

    ErrorType(Class<? extends ErrorHandler> handlerClass) {
        this.handlerClass = handlerClass;
    }
}

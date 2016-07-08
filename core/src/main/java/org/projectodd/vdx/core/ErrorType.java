package org.projectodd.vdx.core;

import org.projectodd.vdx.core.handlers.DuplicateAttributeHandler;
import org.projectodd.vdx.core.handlers.DuplicateElementHandler;
import org.projectodd.vdx.core.handlers.InvalidAttributeValueHandler;
import org.projectodd.vdx.core.handlers.RequiredAttributeMissingHandler;
import org.projectodd.vdx.core.handlers.RequiredElementMissingHandler;
import org.projectodd.vdx.core.handlers.RequiredElementsMissingHandler;
import org.projectodd.vdx.core.handlers.UnexpectedAttributeHandler;
import org.projectodd.vdx.core.handlers.UnexpectedElementEndHandler;
import org.projectodd.vdx.core.handlers.UnexpectedElementHandler;
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
    UNEXPECTED_ELEMENT_END(UnexpectedElementEndHandler.class),
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

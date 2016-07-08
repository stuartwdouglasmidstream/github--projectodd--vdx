package org.projectodd.vdx.core.handlers;

import org.projectodd.vdx.core.ErrorHandler;
import org.projectodd.vdx.core.ValidationContext;
import org.projectodd.vdx.core.ValidationError;

public class InvalidAttributeValueHandler implements ErrorHandler {
    @Override
    public HandledResult handle(ValidationContext ctx, ValidationError err) {
        return null;
    }
}

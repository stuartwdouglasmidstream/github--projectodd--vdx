package org.projectodd.vdx.core.handlers;

import javax.xml.stream.Location;

import org.projectodd.vdx.core.ErrorHandler;
import org.projectodd.vdx.core.I18N;
import org.projectodd.vdx.core.Message;
import org.projectodd.vdx.core.ValidationContext;
import org.projectodd.vdx.core.ValidationError;

public class DuplicateAttributeHandler implements ErrorHandler {
    @Override
    public HandledResult handle(ValidationContext ctx, ValidationError error) {
        final Location loc = error.location();
        final String attr = error.attribute().getLocalPart();
        final String el = error.element().getLocalPart();

        return new HandledResult(loc.getLineNumber(),
                                 loc.getColumnNumber(),
                                 new Message(I18N.Key.ATTRIBUTE_DUPLICATED, attr, el),
                                 null);
    }
}

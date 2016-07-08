package org.projectodd.vdx.core.handlers;

import javax.xml.stream.Location;

import org.projectodd.vdx.core.ErrorHandler;
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
                                 String.format("'%s' can't appear more than once on the '%s' element", attr, el),
                                 null);
    }
}

package org.projectodd.vdx.core.handlers;

import java.util.List;
import java.util.regex.Pattern;

import javax.xml.stream.Location;

import org.projectodd.vdx.core.ErrorHandler;
import org.projectodd.vdx.core.Util;
import org.projectodd.vdx.core.ValidationContext;
import org.projectodd.vdx.core.ValidationError;

public class UnexpectedAttributeHandler implements ErrorHandler {


    @Override
    public HandledResult handle(ValidationContext ctx, ValidationError error) {
        final Location loc = error.location();
        final String attr = error.attribute().getLocalPart();
        final String el = error.element().getLocalPart();
        final ValidationContext.Position pos = ctx.searchForward(loc.getLineNumber() - 1, loc.getColumnNumber(),
                                                                 Pattern.compile(attr + "\\s*="));
        final List<String> altElements = Util.asSortedList(ctx.alternateElementsForAttribute(attr));

        String extra = null;

        if (!altElements.isEmpty()) {
            extra = String.format("'%s' is allowed on elements: %s\nDid you intend to put it on one of those elements?",
                                  attr,
                                  String.join(", ", altElements));
        } else {
            final List<String> otherAttributes = Util.asSortedList(error.alternatives() != null ?
                                                                           error.alternatives() :
                                                                           ctx.attributesForElement(error.element()));
            if (!otherAttributes.isEmpty()) {
                final String altSpelling = Util.alternateSpelling(attr, otherAttributes);

                if (altSpelling != null) {
                    extra = String.format("Did you mean '%s'?", altSpelling);
                } else {
                    extra = String.format("legal attributes are: %s", String.join(", ", otherAttributes));
                }
            }
        }

        return new HandledResult(pos != null ? pos.line : loc.getLineNumber(),
                                 pos != null ? pos.col : loc.getColumnNumber(),
                                 String.format("'%s' isn't an allowed attribute for the '%s' element", attr, el),
                                 extra);
    }
}

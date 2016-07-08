package org.projectodd.vdx.core.handlers;

import java.util.List;
import java.util.regex.Pattern;

import javax.xml.stream.Location;

import org.projectodd.vdx.core.ErrorHandler;
import org.projectodd.vdx.core.Util;
import org.projectodd.vdx.core.ValidationContext;
import org.projectodd.vdx.core.ValidationError;

public class UnexpectedElementHandler implements ErrorHandler {
    @Override
    public HandledResult handle(ValidationContext ctx, ValidationError error) {
        final Location loc = error.location();
        final String el = error.element().getLocalPart();

        //String extra;

        // TODO: have a way to detect where the element could go
        // TODO: find legal elements for current parent (do we know enough to do this?)

       /* if (!altElements.isEmpty()) {
            extra = String.format("'%s' is allowed on elements: %s\nDid you intend to put it on one of those elements?",
                                  attr,
                                  String.join(", ", altElements));
        } else {
            final List<String> otherAttributes = error. ctx.attributesForElement(error.element());

            final String altSpelling = Util.alternateSpelling(attr, otherAttributes);

            if (altSpelling != null) {
                extra = String.format("Did you mean '%s'?", altSpelling);
            } else {
                extra = String.format("legal attributes are: %s", String.join(", ", otherAttributes));
            }
        }*/

        return new HandledResult(loc.getLineNumber(),
                                 loc.getColumnNumber(),
                                 String.format("'%s' isn't an allowed attribute for the '%s' element", el, el),
                                 null); //extra);
    }
}

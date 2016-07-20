package org.projectodd.vdx.core.handlers;

import java.util.List;
import java.util.Set;

import javax.xml.namespace.QName;
import javax.xml.stream.Location;

import org.projectodd.vdx.core.ErrorHandler;
import org.projectodd.vdx.core.Util;
import org.projectodd.vdx.core.ValidationContext;
import org.projectodd.vdx.core.ValidationError;

public class UnexpectedElementHandler implements ErrorHandler {
    @Override
    public HandledResult handle(ValidationContext ctx, ValidationError error) {
        final Location loc = error.location();
        final QName el = error.element();
        final String elName = el.getLocalPart();
        final Set<List<String>> altElements = ctx.alternateElementsForElement(el);

        String extra = null;

        if (!altElements.isEmpty()) {
            extra = String.format("'%s' is allowed in elements: %s\nDid you intend to put it in one of those elements?",
                                  elName,
                                  Util.pathsToString(altElements));
        } else {
            // TODO: find legal elements for current parent (do we know enough to do this?)
            final List<String> otherElements = Util.asSortedList(error.alternatives());

            if (!otherElements.isEmpty()) {
                final String altSpelling = Util.alternateSpelling(elName, otherElements);

                if (altSpelling != null) {
                    extra = String.format("Did you mean '%s'?", altSpelling);
                } else {
                    extra = String.format("elements allowed here are: %s", String.join(", ", otherElements));
                }
            }
        }

        return new HandledResult(loc.getLineNumber(),
                                 loc.getColumnNumber(),
                                 String.format("'%s' isn't an allowed element here", elName),
                                 extra);
    }
}

package org.projectodd.vdx.core.handlers;

import java.util.List;
import java.util.regex.Pattern;

import javax.xml.stream.Location;

import org.projectodd.vdx.core.ErrorHandler;
import org.projectodd.vdx.core.I18N;
import org.projectodd.vdx.core.Message;
import org.projectodd.vdx.core.Position;
import org.projectodd.vdx.core.schema.SchemaElement;
import org.projectodd.vdx.core.Util;
import org.projectodd.vdx.core.ValidationContext;
import org.projectodd.vdx.core.ValidationError;

public class UnexpectedAttributeHandler implements ErrorHandler {


    @Override
    public HandledResult handle(ValidationContext ctx, ValidationError error) {
        final Location loc = error.location();
        final String attr = error.attribute().getLocalPart();
        final String el = error.element().getLocalPart();
        final Position pos = ctx.searchForward(loc.getLineNumber() - 1, loc.getColumnNumber(),
                                               Pattern.compile(attr + "\\s*="));
        final List<List<SchemaElement>> altElements = ctx.alternateElementsForAttribute(attr);

        Message extra = null;

        if (!altElements.isEmpty()) {
            extra = new Message(I18N.Key.ATTRIBUTE_IS_ALLOWED_ON, attr, altElements);
        } else {
            final List<String> otherAttributes = Util.asSortedList(!error.alternatives().isEmpty() ?
                                                                           error.alternatives() :
                                                                           ctx.attributesForElement(error.element()));
            if (!otherAttributes.isEmpty()) {
                final String altSpelling = Util.alternateSpelling(attr, otherAttributes);

                if (altSpelling != null) {
                    extra = new Message(I18N.Key.DID_YOU_MEAN, altSpelling);
                } else {
                    extra = new Message(I18N.Key.ATTRIBUTES_ALLOWED_HERE, otherAttributes);
                }
            }
        }

        final HandledResult result = HandledResult.from(error)
                .message(I18N.Key.ATTRIBUTE_NOT_ALLOWED, attr, el)
                .extraMessage(extra);

        if (pos != null) {
            result.line(pos.line).column(pos.col);
        }

        return result;
    }
}

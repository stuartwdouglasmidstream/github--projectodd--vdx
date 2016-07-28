package org.projectodd.vdx.core.handlers;

import java.util.List;

import javax.xml.namespace.QName;
import javax.xml.stream.Location;

import org.projectodd.vdx.core.ErrorHandler;
import org.projectodd.vdx.core.I18N;
import org.projectodd.vdx.core.Message;
import org.projectodd.vdx.core.SchemaElement;
import org.projectodd.vdx.core.Util;
import org.projectodd.vdx.core.ValidationContext;
import org.projectodd.vdx.core.ValidationError;

public class UnexpectedElementHandler implements ErrorHandler {
    @Override
    public HandledResult handle(ValidationContext ctx, ValidationError error) {
        final Location loc = error.location();
        final QName el = error.element();
        final String elName = el.getLocalPart();
        final List<List<SchemaElement>> altElements = ctx.alternateElementsForElement(el);

        Message extra = null;

        if (!altElements.isEmpty()) {
            extra = new Message(I18N.Key.ELEMENT_IS_ALLOWED_ON,
                                elName,
                                altElements);
        } else {
            // TODO: find legal elements for current parent (do we know enough to do this?)
            final List<String> otherElements = Util.asSortedList(error.alternatives());

            if (!otherElements.isEmpty()) {
                final String altSpelling = Util.alternateSpelling(elName, otherElements);

                if (altSpelling != null) {
                    extra = new Message(I18N.Key.DID_YOU_MEAN, altSpelling);
                } else {
                    extra = new Message(I18N.Key.ELEMENTS_ALLOWED_HERE, otherElements);
                }
            }
        }

        return new HandledResult(loc.getLineNumber(),
                                 loc.getColumnNumber(),
                                 new Message(I18N.Key.ELEMENT_NOT_ALLOWED, elName),
                                 extra);
    }
}

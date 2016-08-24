package org.projectodd.vdx.core.handlers;

import java.util.regex.Pattern;

import javax.xml.stream.Location;

import org.projectodd.vdx.core.ErrorHandler;
import org.projectodd.vdx.core.I18N;
import org.projectodd.vdx.core.Position;
import org.projectodd.vdx.core.ValidationContext;
import org.projectodd.vdx.core.ValidationError;

public class InvalidAttributeValueHandler implements ErrorHandler {
    @Override
    public HandledResult handle(ValidationContext ctx, ValidationError error) {
        final Location loc = error.location();
        final String attr = error.attribute().getLocalPart();
        final Position pos = ctx.searchForward(loc.getLineNumber() - 1, loc.getColumnNumber(),
                                               Pattern.compile(attr + "\\s*="));
        final HandledResult result = HandledResult.from(error)
                .primaryMessage(I18N.Key.ATTRIBUTE_INVALID_VALUE, error.attributeValue(), attr);

        if (pos != null) {
            result.line(pos.line).column(pos.col);
        }

        return result;
    }
}

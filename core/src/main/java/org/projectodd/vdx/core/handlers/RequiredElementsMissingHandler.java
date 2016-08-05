package org.projectodd.vdx.core.handlers;

import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import javax.xml.stream.Location;

import org.projectodd.vdx.core.ErrorHandler;
import org.projectodd.vdx.core.I18N;
import org.projectodd.vdx.core.Position;
import org.projectodd.vdx.core.Util;
import org.projectodd.vdx.core.ValidationContext;
import org.projectodd.vdx.core.ValidationError;

public class RequiredElementsMissingHandler implements ErrorHandler {
    @Override
    public HandledResult handle(ValidationContext ctx, ValidationError error) {
        final String el = error.element().getLocalPart();
        final Set<String> alts = error.alternatives();
        final Location loc = error.location();
        final Position pos = ctx.searchBackward(loc.getLineNumber() - 1, loc.getColumnNumber(),
                                                Pattern.compile(String.format("<%s[ >/]", el)));

        final HandledResult result = HandledResult.from(error)
                .message(I18N.Key.ELEMENTS_REQUIRED_MISSING, el);

        if (pos != null) {
            result.line(pos.line).column(pos.col);
        }

        if (!alts.isEmpty()) {
            result.extraMessage(I18N.Key.ELEMENTS_REQUIRED_MISSING_LIST, el,
                                Util.asSortedList(alts).stream()
                                        .map(String::toLowerCase)
                                        .collect(Collectors.toList()));
        }

        return result;
    }
}

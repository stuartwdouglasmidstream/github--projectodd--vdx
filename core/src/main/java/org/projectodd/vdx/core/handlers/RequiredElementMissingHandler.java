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
import org.projectodd.vdx.core.schema.SchemaElement;

public class RequiredElementMissingHandler implements ErrorHandler {
    public RequiredElementMissingHandler() {
        this(I18N.Key.ELEMENT_REQUIRED_MISSING, I18N.Key.ELEMENT_REQUIRED_MISSING_LIST);
    }

    protected RequiredElementMissingHandler(final I18N.Key primaryMessageKey, final I18N.Key optionsMessageKey) {
        this.primaryMessageKey = primaryMessageKey;
        this.optionsMessageKey = optionsMessageKey;
    }

    @Override
    public HandledResult handle(ValidationContext ctx, ValidationError error) {
        final String el = error.element().getLocalPart();
        final Set<String> alts = error.alternatives();
        final Location loc = error.location();
        final Position pos = ctx.searchBackward(loc.getLineNumber() - 1, loc.getColumnNumber(),
                                                Pattern.compile(String.format("<%s[ >/]", el)));

        final HandledResult result = HandledResult.from(error)
                .primaryMessage(this.primaryMessageKey, el);

        if (pos != null) {
            result.line(pos.line).column(pos.col);
        }

        if (!alts.isEmpty()) {
            Set<String> otherElements =
                    ctx.elementsForElement(ctx.mapDocLocationToSchemaPath(error.element(), error.position())).stream()
                    .map(SchemaElement::name)
                    .collect(Collectors.toSet());

            result.primaryMessage(this.optionsMessageKey,
                                  Util.asSortedList(alts).stream()
                                          .map(String::toLowerCase)
                                          .map(Util.possiblyUnderscoredName(otherElements))
                                          .collect(Collectors.toList()));
        }

        return result;
    }

    private final I18N.Key primaryMessageKey;
    private final I18N.Key optionsMessageKey;
}

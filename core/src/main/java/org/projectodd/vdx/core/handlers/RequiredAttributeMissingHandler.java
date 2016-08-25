package org.projectodd.vdx.core.handlers;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.projectodd.vdx.core.DocElement;
import org.projectodd.vdx.core.ErrorHandler;
import org.projectodd.vdx.core.I18N;
import org.projectodd.vdx.core.Util;
import org.projectodd.vdx.core.ValidationContext;
import org.projectodd.vdx.core.ValidationError;

public class RequiredAttributeMissingHandler implements ErrorHandler {
    @Override
    public HandledResult handle(ValidationContext ctx, ValidationError error) {
        final String el = error.element().getLocalPart();
        final Set<String> alts = error.alternatives();

        final HandledResult result = HandledResult.from(error)
                .primaryMessage(I18N.Key.ATTRIBUTE_REQUIRED_MISSING, el);

        if (!alts.isEmpty()) {
            final Set<String> attributesForElement =
                    ctx.attributesForElement(ctx.mapDocLocationToSchemaPath(error.element(), error.position()));

            result.primaryMessage(I18N.Key.ATTRIBUTE_REQUIRED_MISSING_LIST,
                                  Util.asSortedList(alts).stream()
                                          .map(String::toLowerCase)
                                          .map(Util.possiblyUnderscoredName(attributesForElement))
                                          .collect(Collectors.toList()));
        }

        return result;
    }
}

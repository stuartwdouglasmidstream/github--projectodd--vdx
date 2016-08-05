package org.projectodd.vdx.core.handlers;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.projectodd.vdx.core.DocElement;
import org.projectodd.vdx.core.ErrorHandler;
import org.projectodd.vdx.core.I18N;
import org.projectodd.vdx.core.ValidationContext;
import org.projectodd.vdx.core.ValidationError;

public class UnsupportedElementHandler implements ErrorHandler {
    @Override
    public HandledResult handle(ValidationContext ctx, ValidationError error) {
        final String el = error.element().getLocalPart();
        final Set<String> alts = error.alternatives();

        return HandledResult.from(error)
                .message(I18N.Key.ELEMENT_UNSUPPORTED, el, alts.stream().findFirst().orElse(null));
    }
}

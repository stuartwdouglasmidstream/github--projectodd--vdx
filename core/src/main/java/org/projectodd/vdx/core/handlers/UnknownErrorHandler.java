package org.projectodd.vdx.core.handlers;

import org.projectodd.vdx.core.ErrorHandler;
import org.projectodd.vdx.core.I18N;
import org.projectodd.vdx.core.ValidationContext;
import org.projectodd.vdx.core.ValidationError;

public class UnknownErrorHandler implements ErrorHandler {
    @Override
    public HandledResult handle(ValidationContext ctx, ValidationError err) {
        return HandledResult.from(err)
                .primaryMessage(I18N.Key.PASSTHRU,
                                err.fallbackMessage() != null ? err.fallbackMessage() : err.message());
    }
}

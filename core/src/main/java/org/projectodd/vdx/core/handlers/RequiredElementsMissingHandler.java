package org.projectodd.vdx.core.handlers;

import org.projectodd.vdx.core.I18N;

public class RequiredElementsMissingHandler extends RequiredElementMissingHandler {
    public RequiredElementsMissingHandler() {
        super(I18N.Key.ELEMENTS_REQUIRED_MISSING, I18N.Key.ELEMENTS_REQUIRED_MISSING_LIST);
    }
}

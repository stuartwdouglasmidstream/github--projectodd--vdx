/*
 * Copyright 2016 Red Hat, Inc, and individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.projectodd.vdx.core.handlers;

import java.util.regex.Pattern;

import org.projectodd.vdx.core.ErrorHandler;
import org.projectodd.vdx.core.I18N;
import org.projectodd.vdx.core.Util;
import org.projectodd.vdx.core.ValidationContext;
import org.projectodd.vdx.core.ValidationError;

public class UnknownErrorHandler implements ErrorHandler {
    @Override
    public HandledResult handle(ValidationContext ctx, ValidationError err) {
        String msg = err.fallbackMessage();
        if (msg == null) {
            msg = err.message();
            if (Pattern.matches(".*\\n at.*", msg)) {
                final String[] parts = msg.split("\\n");
                msg = parts[0];
            }
        }

        return HandledResult.from(err)
                .addPrimaryMessage(I18N.Key.PASSTHRU, Util.stripPeriod(msg));
    }
}

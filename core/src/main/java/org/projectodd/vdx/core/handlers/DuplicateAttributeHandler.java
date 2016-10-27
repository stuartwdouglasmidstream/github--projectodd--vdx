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

import javax.xml.stream.Location;

import org.projectodd.vdx.core.ErrorHandler;
import org.projectodd.vdx.core.I18N;
import org.projectodd.vdx.core.Position;
import org.projectodd.vdx.core.ValidationContext;
import org.projectodd.vdx.core.ValidationError;

public class DuplicateAttributeHandler implements ErrorHandler {
    @Override
    public HandledResult handle(ValidationContext ctx, ValidationError error) {
        final String attr = error.attribute().getLocalPart();
        final String el = error.element() == null ? null : error.element().getLocalPart();
        final Location loc = error.location();
        final Pattern attrPattern = Pattern.compile(String.format("\\s%s\\s*=", attr));
        final Position pos = ctx.searchBackward(loc.getLineNumber() - 1, loc.getColumnNumber(), attrPattern);

        final HandledResult result = HandledResult.from(error);

        if (el == null) {
            result.addPrimaryMessage(I18N.Key.ATTRIBUTE_DUPLICATED_NO_ELEMENT, attr);
        } else {
            result.addPrimaryMessage(I18N.Key.ATTRIBUTE_DUPLICATED, attr, el);
        }

        if (pos != null) {
            result.line(pos.line).column(pos.col + 1);

            // search forward in the current line, from the start
            Position firstPos = ctx.searchForward(pos.line - 1, 0, attrPattern);

            if (firstPos == null ||
                    firstPos.equals(pos)) {
                // nothing found or we found the one we already know about, search backward from the previous line
                firstPos = ctx.searchBackward(pos.line - 2, Integer.MAX_VALUE, attrPattern);
            }

            if (firstPos != null) {
                result.addSecondaryMessage(I18N.Key.ATTRIBUTE_DUPLICATED_FIRST_OCCURRENCE, attr)
                        .addSecondaryResult(new HandledResult(firstPos.line, firstPos.col + 1, null));
            }
        }

        return result;
    }
}

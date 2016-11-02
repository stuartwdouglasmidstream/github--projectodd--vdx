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

import java.util.List;
import java.util.regex.Pattern;

import javax.xml.stream.Location;

import org.projectodd.vdx.core.ErrorHandler;
import org.projectodd.vdx.core.I18N;
import org.projectodd.vdx.core.Position;
import org.projectodd.vdx.core.schema.SchemaElement;
import org.projectodd.vdx.core.Util;
import org.projectodd.vdx.core.ValidationContext;
import org.projectodd.vdx.core.ValidationError;

public class UnexpectedAttributeHandler implements ErrorHandler {


    @Override
    @SuppressWarnings("unchecked")
    public HandledResult handle(ValidationContext ctx, ValidationError error) {
        final Location loc = error.location();
        final String attr = error.attribute().getLocalPart();
        final String el = error.element().getLocalPart();
        final Position pos = ctx.searchForward(loc.getLineNumber() - 1, loc.getColumnNumber(),
                                               Pattern.compile(attr + "\\s*="));
        final List<List<SchemaElement>> altElements = ctx.alternateElementsForAttribute(attr);
        final HandledResult result = HandledResult.from(error)
                .addPrimaryMessage(I18N.Key.ATTRIBUTE_NOT_ALLOWED, attr, el);

        if (pos != null) {
            result.line(pos.line).column(pos.col);
        }

        final List<String> otherAttributes;

        if (error.alternatives().isEmpty()) {
            final List<SchemaElement> schemaPath = ctx.mapDocLocationToSchemaPath(error.element(), error.position());
            if (schemaPath.isEmpty()) {
                result.possiblyMalformed(true);
            }

            otherAttributes = Util.asSortedList(ctx.attributesForElement(schemaPath));
        } else {
            otherAttributes = Util.asSortedList(error.alternatives());
        }

        if (!altElements.isEmpty()) {
            result.addSecondaryMessage(I18N.Key.ATTRIBUTE_IS_ALLOWED_ON, attr, altElements);
        }

        if (otherAttributes.isEmpty()) {
            result.addPrimaryMessage(I18N.Key.ELEMENT_HAS_NO_ATTRIBUTES, el);
        } else {
            final String altSpelling = Util.alternateSpelling(attr, otherAttributes);
            if (altSpelling != null) {
                result.addPrimaryMessage(I18N.Key.DID_YOU_MEAN, altSpelling);
            }

            result.addPrimaryMessage(I18N.Key.ATTRIBUTES_ALLOWED_HERE, otherAttributes);
        }

        return result;
    }
}

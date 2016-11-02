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
import java.util.Set;
import java.util.stream.Collectors;

import org.projectodd.vdx.core.ErrorHandler;
import org.projectodd.vdx.core.I18N;
import org.projectodd.vdx.core.Util;
import org.projectodd.vdx.core.ValidationContext;
import org.projectodd.vdx.core.ValidationError;
import org.projectodd.vdx.core.schema.SchemaElement;

public class RequiredAttributeMissingHandler implements ErrorHandler {
    @Override
    public HandledResult handle(ValidationContext ctx, ValidationError error) {
        final String el = error.element().getLocalPart();
        final Set<String> alts = error.alternatives();

        final HandledResult result = HandledResult.from(error)
                .addPrimaryMessage(I18N.Key.ATTRIBUTE_REQUIRED_MISSING, el);

        if (!alts.isEmpty()) {
            final List<SchemaElement> path = ctx.mapDocLocationToSchemaPath(error.element(), error.position());
            if (path.isEmpty()) {
                result.possiblyMalformed(true);
            }

            final Set<String> attributesForElement = ctx.attributesForElement(path);

            result.addPrimaryMessage(I18N.Key.ATTRIBUTE_REQUIRED_MISSING_LIST,
                                  Util.asSortedList(alts).stream()
                                          .map(String::toLowerCase)
                                          .map(Util.possiblyUnderscoredName(attributesForElement))
                                          .collect(Collectors.toList()));
        }

        return result;
    }
}

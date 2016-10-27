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

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import javax.xml.namespace.QName;

import org.projectodd.vdx.core.DocElement;
import org.projectodd.vdx.core.ErrorHandler;
import org.projectodd.vdx.core.ErrorType;
import org.projectodd.vdx.core.I18N;
import org.projectodd.vdx.core.schema.SchemaElement;
import org.projectodd.vdx.core.Util;
import org.projectodd.vdx.core.ValidationContext;
import org.projectodd.vdx.core.ValidationError;

public class UnexpectedElementHandler implements ErrorHandler {
    @Override
    public HandledResult handle(ValidationContext ctx, ValidationError error) {
        final QName el = error.element();
        final String elName = el.getLocalPart();
        final List<List<SchemaElement>> altElements = ctx.alternateElementsForElement(el);

        // check to see if this is really a duplicate element by getting the ctx.pathToDocElement() that
        // encloses the location. Then, if any siblings with the same name exist, punt to DuplicateElementHandler
        final List<DocElement> path =
                ctx.pathToDocElement(e -> e.qname().equals(el) && e.encloses(error.position()));

        if (!path.isEmpty()) {
            if (!ctx.docElementSiblings(path, e -> e.qname().equals(el)).isEmpty()) {

                return new DuplicateElementHandler().handle(ctx,
                                                            ValidationError.from(error, ErrorType.DUPLICATE_ELEMENT)
                                                                    .element(error.element()));
            }

        }

        final HandledResult response = HandledResult.from(error)
                .addPrimaryMessage(I18N.Key.ELEMENT_NOT_ALLOWED, elName);

        List<String> otherElements = Collections.emptyList();

        if (error.alternatives().isEmpty()) {
            List<DocElement> pathToDocElement = ctx.pathToDocElement(el, error.position());

            if (!pathToDocElement.isEmpty()) {
                final List<SchemaElement> schemaPath =
                        ctx.mapDocPathToSchemaPath(pathToDocElement.subList(0, pathToDocElement.size() - 1));
                if (!schemaPath.isEmpty()) {
                    otherElements =
                            Util.asSortedList(ctx.elementsForElement(schemaPath).stream()
                                                      .map(SchemaElement::name)
                                                      .collect(Collectors.toList()));
                }
            }
        } else {
            otherElements = Util.asSortedList(error.alternatives());
        }

        if (!altElements.isEmpty()) {
            response.addSecondaryMessage(I18N.Key.ELEMENT_IS_ALLOWED_ON,
                                      elName,
                                      altElements);
        }

        if (!otherElements.isEmpty()) {
            final String altSpelling = Util.alternateSpelling(elName, otherElements);

            if (altSpelling != null) {
                response.addPrimaryMessage(I18N.Key.DID_YOU_MEAN, altSpelling);
            }

            response.addPrimaryMessage(I18N.Key.ELEMENTS_ALLOWED_HERE, otherElements);
        }

        return response;
    }
}

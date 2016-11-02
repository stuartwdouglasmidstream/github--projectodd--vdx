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

import org.projectodd.vdx.core.DocElement;
import org.projectodd.vdx.core.ErrorHandler;
import org.projectodd.vdx.core.I18N;
import org.projectodd.vdx.core.ValidationContext;
import org.projectodd.vdx.core.ValidationError;

public class DuplicateElementHandler implements ErrorHandler {
    @Override
    public HandledResult handle(ValidationContext ctx, ValidationError error) {
        final String el = error.element().getLocalPart();
        final String attr = error.attribute() != null ? error.attribute().getLocalPart() : null;
        final String attrValue = error.attributeValue();

        final HandledResult result = HandledResult.from(error);

        final List<DocElement> docPath = ctx.pathToDocElement(error.element(), error.position());
        if (attr != null) {
            result.addPrimaryMessage(I18N.Key.ELEMENT_WITH_ATTRIBUTE_DUPLICATED, el, attr, attrValue);
        } else {
            String parentName = "parent";

            if (!docPath.isEmpty() && docPath.size() > 1) {
                parentName = docPath.get(docPath.size() - 2).name();
            }

            result.addPrimaryMessage(I18N.Key.ELEMENT_DUPLICATED, el, parentName);
        }

        if (!docPath.isEmpty()) {
            final List<List<DocElement>> docElements =
                    ctx.docElementSiblings(docPath, e -> e.qname().equals(error.element()) &&
                            (attr == null || attrValue.equals(e.attributes().get(attr))));

            if (!docElements.isEmpty()) {
                final List<DocElement> firstPath = docElements.get(0);
                if (!firstPath.isEmpty()) {
                    final DocElement otherEl = firstPath.get(firstPath.size() - 1);
                    if (attr != null) {
                        result.addSecondaryMessage(I18N.Key.ELEMENT_WITH_ATTRIBUTE_DUPLICATED_FIRST_OCCURRENCE, el, attr);
                    } else {
                        result.addSecondaryMessage(I18N.Key.ELEMENT_DUPLICATED_FIRST_OCCURRENCE, el);
                    }
                    result.addSecondaryResult(new HandledResult(otherEl.startPosition().line, otherEl.startPosition().col, null));
                }
            }
        } else {
            result.possiblyMalformed(true);
        }

        return result;
    }
}

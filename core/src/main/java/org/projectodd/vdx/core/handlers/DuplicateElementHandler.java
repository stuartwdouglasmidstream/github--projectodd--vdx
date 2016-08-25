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

        if (attr != null) {
            result.primaryMessage(I18N.Key.ELEMENT_WITH_ATTRIBUTE_DUPLICATED, el, attr, attrValue);
        } else {
            result.primaryMessage(I18N.Key.ELEMENT_DUPLICATED, el);
        }

        final List<DocElement> path = ctx.pathToDocElement(error.element(), error.position());

        if (!path.isEmpty()) {
            final List<List<DocElement>> docElements =
                    ctx.docElementSiblings(path, e -> e.qname().equals(error.element()) &&
                            (attr == null || attrValue.equals(e.attributes().get(attr))));

            if (!docElements.isEmpty()) {
                final List<DocElement> firstPath = docElements.get(0);
                if (!firstPath.isEmpty()) {
                    final DocElement otherEl = firstPath.get(firstPath.size() - 1);
                    if (attr != null) {
                        result.secondaryMessage(I18N.Key.ELEMENT_WITH_ATTRIBUTE_DUPLICATED_FIRST_OCCURRENCE, el, attr);
                    } else {
                        result.secondaryMessage(I18N.Key.ELEMENT_DUPLICATED_FIRST_OCCURRENCE, el);
                    }
                    result.secondaryResult(new HandledResult(otherEl.startPosition().line, otherEl.startPosition().col, null)
                                               .primaryMessage(I18N.Key.BLANK));
                }
            }
        }

        return result;
    }
}

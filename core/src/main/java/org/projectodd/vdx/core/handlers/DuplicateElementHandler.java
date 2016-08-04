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
        final String attr = error.attribute().getLocalPart();
        final String attrValue = error.attributeValue();

        final List<List<DocElement>> docElements =
                ctx.pathsToDocElement(e -> e.qname().equals(error.element()) &&
                        attrValue.equals(e.attributes().get(attr)));

        final HandledResult result = HandledResult.from(error)
                .message(I18N.Key.ELEMENT_DUPLICATED, el, attr, attrValue);

        if (!docElements.isEmpty()) {
            final List<DocElement> firstPath = docElements.get(0);
            if (!firstPath.isEmpty()) {
                final DocElement otherEl = firstPath.get(firstPath.size() - 1);
                result.extraMessage(I18N.Key.ELEMENT_DUPLICATED_FIRST_OCCURRENCE, el, attr)
                .extraResult(new HandledResult(otherEl.startPosition().line, otherEl.startPosition().col, null)
                                   .message(I18N.Key.BLANK));
            }
        }

        return result;
    }
}

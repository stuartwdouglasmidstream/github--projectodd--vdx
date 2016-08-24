package org.projectodd.vdx.core.handlers;

import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import javax.xml.namespace.QName;
import javax.xml.stream.Location;

import org.projectodd.vdx.core.DocElement;
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
                .primaryMessage(I18N.Key.ATTRIBUTE_NOT_ALLOWED, attr, el);

        if (pos != null) {
            result.line(pos.line).column(pos.col);
        }

        final List<String> otherAttributes;

        if (error.alternatives().isEmpty()) {
            final List<QName> pathFromDoc =
                    ctx.pathToDocElement(e -> e.qname().equals(error.element()) && e.encloses(error.position())).stream()
                            .map(DocElement::qname)
                            .collect(Collectors.toList());
            final List<SchemaElement> schemaPath = ctx.pathsToSchemaElement(e -> e.qname().equals(error.element())).stream()
                    .filter(p -> ctx.schemaPathWithPrefix(p).stream()
                            .map(SchemaElement::qname)
                            .collect(Collectors.toList())
                            .equals(pathFromDoc))
                    .findFirst()
                    .orElse(Collections.EMPTY_LIST);

            otherAttributes = Util.asSortedList(ctx.attributesForElement(schemaPath));
        } else {
            otherAttributes = Util.asSortedList(error.alternatives());
        }

        if (!altElements.isEmpty()) {
            result.secondaryMessage(I18N.Key.ATTRIBUTE_IS_ALLOWED_ON, attr, altElements);
        }

        if (otherAttributes.isEmpty()) {
            result.secondaryMessage(I18N.Key.ELEMENT_HAS_NO_ATTRIBUTES, el);
        } else {
            result.primaryMessage(I18N.Key.ATTRIBUTES_ALLOWED_HERE, otherAttributes);

            final String altSpelling = Util.alternateSpelling(attr, otherAttributes);
            if (altSpelling != null) {
                result.primaryMessage(I18N.Key.DID_YOU_MEAN, altSpelling);
            }
        }

        return result;
    }
}

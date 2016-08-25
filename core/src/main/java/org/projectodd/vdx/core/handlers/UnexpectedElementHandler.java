package org.projectodd.vdx.core.handlers;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import javax.xml.namespace.QName;

import org.projectodd.vdx.core.DocElement;
import org.projectodd.vdx.core.ErrorHandler;
import org.projectodd.vdx.core.ErrorType;
import org.projectodd.vdx.core.I18N;
import org.projectodd.vdx.core.Message;
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
                .primaryMessage(I18N.Key.ELEMENT_NOT_ALLOWED, elName);

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
            response.secondaryMessage(I18N.Key.ELEMENT_IS_ALLOWED_ON,
                                      elName,
                                      altElements);
        }

        if (!otherElements.isEmpty()) {
            response.primaryMessage(I18N.Key.ELEMENTS_ALLOWED_HERE, otherElements);

            final String altSpelling = Util.alternateSpelling(elName, otherElements);

            if (altSpelling != null) {
                response.primaryMessage(I18N.Key.DID_YOU_MEAN, altSpelling);
            }
        }

        return response;
    }
}

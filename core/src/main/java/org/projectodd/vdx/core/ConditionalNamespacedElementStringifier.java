package org.projectodd.vdx.core;

import java.util.function.Predicate;

public class ConditionalNamespacedElementStringifier implements Stringifier {
    public ConditionalNamespacedElementStringifier(final Predicate<SchemaElement> pred) {
        this.pred = pred;
    }

    @Override
    public boolean handles(Object value) {
        return Stringifier.super.handles(value) &&
                pred.test((SchemaElement)value);
    }

    @Override
    public Class handledClass() {
        return SchemaElement.class;
    }

    @Override
    public String asString(Object value) {
        final SchemaElement el = (SchemaElement)value;

        return el.name() + " " + el.qname().getNamespaceURI();
    }

    private final Predicate<SchemaElement> pred;
}

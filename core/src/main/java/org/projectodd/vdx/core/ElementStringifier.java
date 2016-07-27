package org.projectodd.vdx.core;

public class ElementStringifier implements Stringifier {
    @Override
    public Class handledClass() {
        return SchemaElement.class;
    }

    @Override
    public String asString(Object value) {
        return ((SchemaElement)value).name();
    }
}

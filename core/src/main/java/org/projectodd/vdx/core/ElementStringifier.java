package org.projectodd.vdx.core;

import org.projectodd.vdx.core.schema.SchemaElement;

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

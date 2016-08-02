package org.projectodd.vdx.core;

import java.util.List;
import java.util.stream.Collectors;

import org.projectodd.vdx.core.schema.SchemaElement;

public class ElementListStringifier implements Stringifier {

    @Override
    @SuppressWarnings("unchecked")
    public boolean handles(Object value) {
        return Stringifier.super.handles(value) &&
                SchemaElement.class.isAssignableFrom(((List)value).get(0).getClass());
    }

    @Override
    public Class handledClass() {
        return List.class;
    }

    @Override
    public String asString(Object value) {
        return String.join(" > ", ((List<?>)value).stream()
                .map(Stringify::asString)
                .collect(Collectors.toList()));
    }
}

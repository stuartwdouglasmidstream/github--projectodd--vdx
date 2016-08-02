package org.projectodd.vdx.core;

import java.util.List;
import java.util.stream.Collectors;

import org.projectodd.vdx.core.schema.SchemaElement;

public class NonElementListStringifier implements Stringifier {

    public NonElementListStringifier(final int limit) {
        this.limit = limit;
    }

    @Override
    @SuppressWarnings("unchecked")
    public boolean handles(Object value) {
        return Stringifier.super.handles(value) &&
                !SchemaElement.class.isAssignableFrom(((List)value).get(0).getClass());
    }

    @Override
    public Class handledClass() {
        return List.class;
    }

    @Override
    public String asString(Object value) {
        final List<?> list = (List<?>)value;

        final List<String> values = list.stream()
                .limit(limit > 0 ? limit : list.size())
                .map(Stringify::asString)
                .collect(Collectors.toList());

        final StringBuilder sb = new StringBuilder();
        sb.append('\n');
        values.forEach(v -> sb.append("- ").append(v).append('\n'));

        if (limit < list.size()) {
            sb.append(String.format("(and %s more)\n", list.size() - limit));
        }

        return sb.toString();
    }

    private final int limit;
}

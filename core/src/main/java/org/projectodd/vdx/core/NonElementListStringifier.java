package org.projectodd.vdx.core;

import java.util.List;
import java.util.stream.Collectors;

import org.projectodd.vdx.core.schema.SchemaElement;

public class NonElementListStringifier implements Stringifier {

    public NonElementListStringifier(final int threshold, final int limit) {
        if (limit <= threshold) {
            throw new IllegalArgumentException("limit must be greater than threshold");
        }

        this.threshold = threshold;
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
                .map(Stringify::asString)
                .distinct()
                .limit(limit > 0 ? limit : list.size())
                .collect(Collectors.toList());

        final StringBuilder sb = new StringBuilder();
        if (values.size() <= this.threshold) {
            sb.append(Util.asCommaString(values));
        } else {
            sb.append('\n');
            values.forEach(v -> sb.append("- ").append(v).append('\n'));

            if (this.limit < list.size()) {
                sb.append(String.format("(and %s more)\n", list.size() - limit));
            }
        }

        return sb.toString();
    }

    private final int threshold;
    private final int limit;
}

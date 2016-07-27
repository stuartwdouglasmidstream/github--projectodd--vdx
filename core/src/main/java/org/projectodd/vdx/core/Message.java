package org.projectodd.vdx.core;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class Message {
    public Message(String template, Object... values) {
        this.template = template;
        this.values = Arrays.asList(values);
    }

    public List<Object> rawValues() {
        return Collections.unmodifiableList(this.values);
    }

    public List<String> stringValues() {
        return this.values.stream()
                .map(Stringify::asString)
                .collect(Collectors.toList());
    }

    public String template() {
        return this.template;
    }

    @Override
    public String toString() {
        return String.format(this.template, stringValues().toArray());
    }

    private final String template;
    private final List<Object> values;
}

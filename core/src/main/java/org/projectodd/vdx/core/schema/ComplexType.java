package org.projectodd.vdx.core.schema;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.xml.namespace.QName;

public class ComplexType {
    public ComplexType(final QName name) {
        this.name = name;
    }

    public QName name() {
        return name;
    }

    public QName base() {
        return base;
    }

    public ComplexType base(final QName base) {
        this.base = base;

        return this;
    }

    public Set<String> attributes() {
        return Collections.unmodifiableSet(attributes);
    }

    public List<SchemaElement> elements() {
        return Collections.unmodifiableList(this.elements);
    }

    public void addAttribute(final String attr) {
        this.attributes.add(attr);
    }

    public void addElement(final SchemaElement el) {
        this.elements.add(el);
    }

    private final QName name;
    private QName base = null;
    private final Set<String> attributes = new HashSet<>();
    private final List<SchemaElement> elements = new ArrayList<>();
}

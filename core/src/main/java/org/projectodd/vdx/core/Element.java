package org.projectodd.vdx.core;

import java.util.Collections;
import java.util.Set;
import java.util.TreeSet;

import javax.xml.namespace.QName;

public class Element {

    public Element(QName name) {
        this.name = name;
    }

    public void addAttribute(String attribute) {
        this.attributes.add(attribute);
    }

    public String name() {
        return name.getLocalPart();
    }

    public QName qname() {
        return this.name;
    }


    public Set<String> attributes() {
        return Collections.unmodifiableSet(attributes);
    }

    @Override
    public String toString() {
        return "<Element name=" + this.name +
                ", attributes=" + this.attributes + ">";
    }

    private final QName name;
    private final Set<String> attributes = new TreeSet<>();
}

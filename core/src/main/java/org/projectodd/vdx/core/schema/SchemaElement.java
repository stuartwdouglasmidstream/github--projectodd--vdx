package org.projectodd.vdx.core.schema;

import java.util.Collection;
import java.util.Collections;
import java.util.Set;
import java.util.TreeSet;

import javax.xml.namespace.QName;

public class SchemaElement {

    public SchemaElement(final QName name, final QName type) {
        this.name = name;
        this.type = type;
        this.reference = false;
    }

    public SchemaElement(final QName name, final boolean reference) {
        this.name = name;
        this.reference = reference;
        this.type = null;
    }

    public SchemaElement(final QName name) {
        this(name, null);
    }

    public SchemaElement delegate(final SchemaElement delegate) {
        this.delegate = delegate;
        this.reference = false;

        return this;
    }

    public void addAttribute(String attribute) {
        if (this.delegate != null) {
            this.delegate.addAttribute(attribute);
        } else {
            this.attributes.add(attribute);
        }
    }

    public void addAttributes(final Collection<String> attrs) {
        if (this.delegate != null) {
            this.delegate.addAttributes(attrs);
        } else {
            this.attributes.addAll(attrs);
        }
    }

    public String name() {
        if (this.delegate != null) {
            return this.delegate.name();
        } else {
            return name.getLocalPart();
        }
    }

    public QName qname() {
        if (this.delegate != null) {
            return this.delegate.qname();
        } else {
            return this.name;
        }
    }

    public QName type() {
        if (this.delegate != null) {
            return this.delegate.type();
        } else {
            return this.type;
        }
    }

    public QName base() {
        if (this.delegate != null) {
            return this.delegate.base();
        } else {
            return this.base;
        }
    }

    public boolean isReference() {
        return reference;
    }

    public SchemaElement base(final QName base) {
        if (this.delegate != null) {
            this.delegate.base(base);
        } else {
            this.base = base;
        }

        return this;
    }

    public Set<String> attributes() {
        if (this.delegate != null) {
            return this.delegate.attributes();
        } else {
            return Collections.unmodifiableSet(attributes);
        }
    }

    @Override
    public String toString() {
        return "<SchemaElement name=" + name() +
                ", attributes=" + attributes() + ">";
    }

    @Override
    public boolean equals(Object obj) {
        if (this.delegate != null) return this.delegate.equals(obj);
        if (obj == null) return false;
        if (obj == this) return true;
        if (!(obj instanceof SchemaElement)) return false;

        final SchemaElement that = (SchemaElement) obj;

        return this.name.equals(that.name)
                && this.attributes.equals(that.attributes);
    }

    @Override
    public int hashCode() {
        if (this.delegate != null) {
            return this.delegate.hashCode();
        } else {
            return this.name.hashCode() +
                    this.attributes.hashCode();
        }
    }

    private final QName name;
    private final QName type;
    private boolean reference;
    private SchemaElement delegate = null;
    private QName base;
    private final Set<String> attributes = new TreeSet<>();
}

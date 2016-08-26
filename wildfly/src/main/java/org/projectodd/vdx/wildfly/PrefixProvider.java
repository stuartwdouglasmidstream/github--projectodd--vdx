package org.projectodd.vdx.wildfly;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.xml.namespace.QName;

import org.projectodd.vdx.core.schema.SchemaPathPrefixProvider;

class PrefixProvider implements SchemaPathPrefixProvider {
    PrefixProvider(final QName rootElement) {
        this.prefix.add(rootElement);
        this.prefix.add(new QName(rootElement.getNamespaceURI(), "profile"));
    }


    @Override
    public List<QName> prefixFor(final List<QName> path) {
        if (this.prefix.get(0).equals(path.get(0))) {

            return Collections.emptyList();
        }

        return Collections.unmodifiableList(prefix);
    }

    private final List<QName> prefix = new ArrayList<>();
}

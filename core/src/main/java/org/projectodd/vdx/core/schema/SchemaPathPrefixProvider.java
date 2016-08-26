package org.projectodd.vdx.core.schema;

import java.util.List;

import javax.xml.namespace.QName;

public interface SchemaPathPrefixProvider {
    List<QName> prefixFor(List<QName> path);
}

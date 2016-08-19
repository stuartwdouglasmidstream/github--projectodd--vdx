package org.projectodd.vdx.core.schema;

import java.util.List;

import javax.xml.namespace.QName;

public interface SchemaPathPrefixFinder {
    List<QName> prefixFor(List<QName> path);
}

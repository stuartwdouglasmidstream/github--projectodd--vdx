/*
 * Copyright 2016 Red Hat, Inc, and individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.projectodd.vdx.core.schema;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.xml.namespace.QName;

import org.projectodd.vdx.core.Tree;

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

    public Tree<SchemaElement> elements() {
        if (this.elements == null) {

            return new Tree<>();
        }

        return this.elements;
    }

    public void setElements(final Tree<SchemaElement> elements) {
        this.elements = elements;
    }

    public void addAttribute(final String attr) {
        this.attributes.add(attr);
    }

    private final QName name;
    private QName base = null;
    private final Set<String> attributes = new HashSet<>();
    private Tree<SchemaElement> elements = null;
}

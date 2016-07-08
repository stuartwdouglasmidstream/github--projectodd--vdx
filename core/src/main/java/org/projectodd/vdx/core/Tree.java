package org.projectodd.vdx.core;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Tree<T> {
    public Tree(T value) {
        this.value = value;
    }

    public void addChild(Tree<T> child) {
        this.children.add(child);
    }

    public T value() {
        return value;
    }

    public List<Tree<T>> children() {
        return Collections.unmodifiableList(children);
    }

    private final T value;
    private final List<Tree<T>> children = new ArrayList<>();
}

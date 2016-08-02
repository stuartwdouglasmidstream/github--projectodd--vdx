package org.projectodd.vdx.core;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Function;

public class Tree<T> {
    public Tree() {
        this(null);
    }

    public Tree(final T value) {
        this.value = value;
    }

    public Tree<T> addChild(Tree<T> child) {
        this.children.add(child);

        return child;
    }

    public Tree<T> addChild(T child) {
        return addChild(new Tree<>(child));
    }

    public T value() {
        return value;
    }

    public List<Tree<T>> children() {
        return Collections.unmodifiableList(children);
    }

    public boolean isRoot() {
        return this.value == null;
    }

    public List<List<T>> pathsToValue(final Function<T, Boolean> pred) {
        return pathsToValue(false, pred);
    }

    public List<List<T>> pathsToValue(final boolean includeValue, final Function<T, Boolean> pred) {
        final List<List<T>> paths = new ArrayList<>();
        if (!isRoot() && pred.apply(this.value)) {
            List<T> path = new ArrayList<>();
            if (includeValue) {
                path.add(this.value);
            }
            paths.add(path);
        }

        this.children().forEach(c -> {
            final List<List<T>> childPaths = c.pathsToValue(includeValue, pred);
            if (!childPaths.isEmpty() &&
                    !isRoot()) {
                childPaths.forEach(p -> p.add(0, this.value));
            }
            paths.addAll(childPaths);
        });

        return paths;
    }

    // TODO: provide a stream instead of implementing our own reduce
    @SuppressWarnings("unchecked")
    public <V> V reduce(final V accum, final BiFunction<V, T, V> s) {
        V ret = accum;

        try {
            if (!isRoot()) {
                ret = s.apply(ret, this.value);
            }

            for(Tree<T> child : this.children) {
                ret = child.reduce(ret, s);
            }
        } catch (ReduceComplete e) {
            ret = (V)e.result;
        }

        return ret;
    }

    public String toString() {
        return "<value=" + this.value +
                ", children=" + this.children + ">";
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) return false;
        if (this == obj) return true;
        if (!(obj instanceof Tree)) return false;

        final Tree that = (Tree) obj;

        return !((this.isRoot() && !that.isRoot()) || (!this.isRoot() && that.isRoot()))
                && !(!this.isRoot() && !this.value.equals(that.value))
                && this.children.equals(that.children);
    }

    @Override
    public int hashCode() {
        return (this.isRoot() ? 0 : this.value.hashCode()) +
                this.children.hashCode();
    }

    public static void reduceComplete(Object result) {
        throw new ReduceComplete(result);
    }

    private static class ReduceComplete extends RuntimeException {
        public final Object result;

        ReduceComplete(final Object result) {
            this.result = result;
        }
    }

    private final T value;
    private final List<Tree<T>> children = new ArrayList<>();
}

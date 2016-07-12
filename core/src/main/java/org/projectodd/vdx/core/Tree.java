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

    Tree(final T value) {
        this.value = value;
    }

    public Tree<T> addChild(Tree<T> child) {
        this.children.add(child);

        return this;
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

    public Set<List<T>> pathsToValue(final Function<T, Boolean> pred) {
        final Set<List<T>> paths = new HashSet<>();
        if (!isRoot() && pred.apply(value)) {
            paths.add(new ArrayList<>());
        }

        this.children().forEach(c -> {
            final Set<List<T>> childPaths = c.pathsToValue(pred);

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

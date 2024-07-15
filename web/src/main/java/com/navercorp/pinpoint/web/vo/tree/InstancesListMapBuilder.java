package com.navercorp.pinpoint.web.vo.tree;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Predicate;

public class InstancesListMapBuilder<T, R> {
    private final Collection<T> collection;

    private final Function<R, String> keyExtractor;
    private final Comparator<String> keyComparator;
    private final Comparator<R> sortNestedListBy;

    // When adding filter Predicates,
    // do not simply add Predicate objects to a list.
    // Instead, explicitly specify the connection relationships
    // (e.g., and, or) between the Predicates.
    private Predicate<T> filter;
    private final Function<T, R> finisher;

    InstancesListMapBuilder(Function<R, String> keyExtractor,
                            Comparator<String> keyComparator,
                            Comparator<R> sortNestedListBy,
                            Collection<T> collection,
                            Function<T, R> finisher) {
        this.keyExtractor = Objects.requireNonNull(keyExtractor, "keyExtractor");
        this.keyComparator = Objects.requireNonNull(keyComparator, "keyComparator");
        this.sortNestedListBy = Objects.requireNonNull(sortNestedListBy, "sortNestedListBy");
        this.collection = Objects.requireNonNull(collection, "collection");
        this.finisher = Objects.requireNonNullElse(finisher, this::castingIdentity);
    }

    public InstancesListMapBuilder<T, R> withFilterBefore(Predicate<T> filter) {
        this.filter = filter;
        return this;
    }

    private boolean filterBefore(T t) {
        if (filter != null) {
            return filter.test(t);
        }
        return true;
    }

    public InstancesListMap<R> build() {
        return InstancesListMap.newAgentsListMap(
                getProcessedCollection(),
                keyExtractor,
                keyComparator,
                sortNestedListBy
        );
    }

    private List<R> getProcessedCollection() {
        List<R> result = new ArrayList<>(collection.size());
        for (T t : collection) {
            if (!filterBefore(t)) {
                continue;
            }

            R r = finisher.apply(t);
            result.add(r);
        }
        return result;
    }

    @SuppressWarnings("unchecked")
    private R castingIdentity(T t) {
        return (R) t;
    }

    @Override
    public String toString() {
        return "InstancesListMapBuilder{" +
                "collection=" + collection +
                ", keyExtractor=" + keyExtractor +
                ", keyComparator=" + keyComparator +
                ", sortNestedListBy=" + sortNestedListBy +
                ", filter=" + filter +
                ", finisher=" + finisher +
                '}';
    }
}

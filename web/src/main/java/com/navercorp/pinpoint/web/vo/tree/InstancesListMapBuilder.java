package com.navercorp.pinpoint.web.vo.tree;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class InstancesListMapBuilder<T, R> {
    private final Collection<T> collection;

    private final Function<R, String> keyExtractor;
    private final Comparator<String> keyComparator;
    private final Comparator<R> sortNestedListBy;

    private Predicate<T> instanceFilter = x -> true;
    private Function<T, R> finisher = this::castingIdentity;

    InstancesListMapBuilder(Function<R, String> keyExtractor,
                            Comparator<String> keyComparator,
                            Comparator<R> sortNestedListBy,
                            Collection<T> collection) {
        this.keyExtractor = Objects.requireNonNull(keyExtractor, "keyExtractor");
        this.keyComparator = Objects.requireNonNull(keyComparator, "keyComparator");
        this.sortNestedListBy = Objects.requireNonNull(sortNestedListBy, "sortNestedListBy");
        this.collection = Objects.requireNonNull(collection, "collection");
    }

    public InstancesListMapBuilder<T, R> withFilter(Predicate<T> filter) {
        this.instanceFilter = filter;
        return this;
    }

    public InstancesListMapBuilder<T, R> withFinisher(Function<T, R> finisher) {
        this.finisher = finisher;
        return this;
    }

    public InstancesListMap<R> build() {
        List<R> stream = collection.stream()
                .filter(instanceFilter)
                .map(finisher)
                .collect(Collectors.toList());

        return InstancesListMap.newAgentsListMap(
                stream,
                keyExtractor,
                keyComparator,
                sortNestedListBy
        );
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
                ", instanceFilter=" + instanceFilter +
                ", finisher=" + finisher +
                '}';
    }
}

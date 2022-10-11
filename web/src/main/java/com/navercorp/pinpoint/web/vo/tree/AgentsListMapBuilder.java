package com.navercorp.pinpoint.web.vo.tree;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class AgentsListMapBuilder<T, R> {
    private final Collection<T> agentCollection;

    private final Function<R, String> keyExtractor;
    private final Comparator<String> keyComparator;
    private final SortBy<R> sortAgentsListBy;

    private Predicate<T> agentFilter = x -> true;
    private Function<T, R> finisher = this::castingIdentity;

    AgentsListMapBuilder(Function<R, String> keyExtractor,
                         Comparator<String> keyComparator,
                         SortBy<R> sortAgentsListBy,
                         Collection<T> agentCollection) {
        this.keyExtractor = Objects.requireNonNull(keyExtractor, "keyExtractor");
        this.keyComparator = Objects.requireNonNull(keyComparator, "keyComparator");
        this.sortAgentsListBy = Objects.requireNonNull(sortAgentsListBy, "sortAgentsListBy");
        this.agentCollection = Objects.requireNonNull(agentCollection, "agentCollection");
    }

    public AgentsListMapBuilder<T, R> withFilter(Predicate<T> filter) {
        this.agentFilter = filter;
        return this;
    }

    public AgentsListMapBuilder<T, R> withFinisher(Function<T, R> finisher) {
        this.finisher = finisher;
        return this;
    }

    public AgentsListMap<R> build() {
        List<R> stream = agentCollection.stream()
                .filter(agentFilter)
                .map(finisher)
                .collect(Collectors.toList());

        return AgentsListMap.newAgentsListMap(
                stream,
                keyExtractor,
                keyComparator,
                sortAgentsListBy
        );
    }

    @SuppressWarnings("unchecked")
    private R castingIdentity(T t) {
        return (R) t;
    }

    @Override
    public String toString() {
        return "AgentsListMapBuilder{" +
                "agentCollection=" + agentCollection +
                ", keyExtractor=" + keyExtractor +
                ", keyComparator=" + keyComparator +
                ", sortAgentsListBy=" + sortAgentsListBy +
                ", agentFilter=" + agentFilter +
                ", finisher=" + finisher +
                '}';
    }
}

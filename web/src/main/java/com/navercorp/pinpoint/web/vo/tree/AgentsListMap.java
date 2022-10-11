package com.navercorp.pinpoint.web.vo.tree;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;
import java.util.function.Function;
import java.util.stream.Collector;
import java.util.stream.Collectors;

public class AgentsListMap<T> {

    private final List<AgentsList<T>> listMap;

    private AgentsListMap(List<AgentsList<T>> listMap) {
        this.listMap = Objects.requireNonNull(listMap, "listMap");
    }

    public static <T> AgentsListMap<T> newAgentsListMap(Collection<T> collection,
                                                        Function<T, String> keyExtractor,
                                                        Comparator<String> keyComparator,
                                                        SortBy<T> sortBy) {
        if (collection.isEmpty()) {
            return empty();
        }

        Collector<T, ?, Map<String, List<T>>> collector = Collectors.groupingBy(keyExtractor);
        Map<String, List<T>> mapByGivenClassifier = collection.stream().collect(collector);

        Map<String, AgentsList<T>> map = mapByGivenClassifier.entrySet().stream().collect(
                Collectors.toMap(
                        Map.Entry::getKey,
                        e -> AgentsList.sorted(e.getKey(), e.getValue(), sortBy.getComparator()),
                        (left, right) -> left,
                        () -> new TreeMap<>(keyComparator)
                )
        );

        List<AgentsList<T>> agentsListMap = new ArrayList<>(map.values());
        return new AgentsListMap<>(agentsListMap);
    }

    public static <T> AgentsListMap<T> empty() {
        return new AgentsListMap<>(new ArrayList<>());
    }

    public List<AgentsList<T>> getListMap() {
        return listMap;
    }

    @Override
    public String toString() {
        return "AgentsListMap{" +
                "listMap=" + listMap +
                '}';
    }
}

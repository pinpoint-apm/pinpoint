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

public class InstancesListMap<T> {

    private final List<InstancesList<T>> listMap;

    private InstancesListMap(List<InstancesList<T>> listMap) {
        this.listMap = Objects.requireNonNull(listMap, "listMap");
    }

    public static <T> InstancesListMap<T> newAgentsListMap(Collection<T> collection,
                                                           Function<T, String> keyExtractor,
                                                           Comparator<String> keyComparator,
                                                           Comparator<T> sortNestedListBy) {
        if (collection.isEmpty()) {
            return empty();
        }

        Collector<T, ?, Map<String, List<T>>> collector = Collectors.groupingBy(keyExtractor);
        Map<String, List<T>> mapByGivenClassifier = collection.stream().collect(collector);

        Map<String, InstancesList<T>> map = mapByGivenClassifier.entrySet().stream().collect(
                Collectors.toMap(
                        Map.Entry::getKey,
                        e -> InstancesList.sorted(e.getKey(), e.getValue(), sortNestedListBy),
                        (left, right) -> left,
                        () -> new TreeMap<>(keyComparator)
                )
        );

        List<InstancesList<T>> instancesListMap = new ArrayList<>(map.values());
        return new InstancesListMap<>(instancesListMap);
    }

    public static <T> InstancesListMap<T> empty() {
        return new InstancesListMap<>(new ArrayList<>());
    }

    public List<InstancesList<T>> getListMap() {
        return listMap;
    }

    @Override
    public String toString() {
        return "InstancesListMap{" +
                "listMap=" + listMap +
                '}';
    }
}

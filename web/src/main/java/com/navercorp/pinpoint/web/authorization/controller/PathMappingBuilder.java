package com.navercorp.pinpoint.web.authorization.controller;

import com.navercorp.pinpoint.web.service.appmetric.MetricName;
import org.springframework.core.annotation.AnnotationUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class PathMappingBuilder<T> {

    private final String prefix;

    public PathMappingBuilder(String prefix) {
        this.prefix = Objects.requireNonNull(prefix, "prefix");
    }

    public Map<String, T> build(List<T> serviceList) {
        Map<String, T> map = new HashMap<>();
        for (T service : serviceList) {
            final String chartType = getChartType(service);

            T duplicate = map.put(chartType, service);
            if (duplicate != null) {
                String errorMessage = String.format("Duplicated ChartService chartType:%s %s:%s", chartType, service, duplicate);
                throw new IllegalArgumentException(errorMessage);
            }
        }

        return Map.copyOf(map);
    }

    protected String getChartType(T service) {
        MetricName name = AnnotationUtils.getAnnotation(service.getClass(), MetricName.class);
        if (name == null) {
            throw new IllegalArgumentException("@MetricName not found for :" + service.getClass().getName());
        }
        return parseChartType(name.value());
    }

    String parseChartType(String alias) {
        if (!alias.startsWith(prefix)) {
            throw new IllegalStateException("unexpected aliasName:" + alias);
        }
        return alias.substring(prefix.length());
    }
}

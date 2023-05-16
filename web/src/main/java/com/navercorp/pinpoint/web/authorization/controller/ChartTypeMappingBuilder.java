package com.navercorp.pinpoint.web.authorization.controller;

import com.navercorp.pinpoint.web.service.stat.ChartTypeSupport;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ChartTypeMappingBuilder<T> {


    public ChartTypeMappingBuilder() {
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
        if (service instanceof ChartTypeSupport) {
            ChartTypeSupport a = (ChartTypeSupport)service;
            return a.getChartType();
        }
        throw new RuntimeException("Unknown ChartTypeSupport " + service);
    }

}

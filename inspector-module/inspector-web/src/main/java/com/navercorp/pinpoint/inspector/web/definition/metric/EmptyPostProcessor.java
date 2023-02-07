package com.navercorp.pinpoint.inspector.web.definition.metric;

import com.navercorp.pinpoint.inspector.web.model.InspectorMetricValue;
import com.navercorp.pinpoint.metric.web.model.MetricValue;

import java.util.List;

public class EmptyPostProcessor implements MetricPostProcessor {

    public static final EmptyPostProcessor INSTANCE = new EmptyPostProcessor();

    private EmptyPostProcessor() {
    }

    @Override
    public String getName() {
        return "empty";
    }

    @Override
    public List<InspectorMetricValue> postProcess(List<InspectorMetricValue> metricValueList) {
        return metricValueList;
    }
}

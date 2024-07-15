package com.navercorp.pinpoint.otlp.web.view;

import java.util.List;

public class OtlpChartExpHistogramViewBuilder extends OtlpChartViewBuilder {

    protected OtlpChartExpHistogramViewBuilder() {
        super(CHART_TYPE_BAR);
    }

    @Override
    protected void checkValidity(int timestampSize) {

    }

    @Override
    protected String checkChartType(String fieldName, String description) {
        return null;
    }

    @Override
    protected void setMetadata(String name, List<Number> values, String description) {
        throw new UnsupportedOperationException("ExpHistogramViewBuilder does not support metadata.");

    }
}

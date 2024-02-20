package com.navercorp.pinpoint.otlp.web.view;

import java.util.List;

public class OtlpChartSumGaugeViewBuilder extends OtlpChartViewBuilder {

    public OtlpChartSumGaugeViewBuilder() {
        super(CHART_TYPE_SPLINE);
        this.hasSummaryField = true;
    }

    @Override
    protected String checkChartType(String fieldName, String description) {
        return this.defaultChartType;
    }

    @Override
    protected void setMetadata(String name, List<Number> values, String description) {
        throw new UnsupportedOperationException("SumGaugeViewBuilder does not support metadata.");
    }
}

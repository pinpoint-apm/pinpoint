package com.navercorp.pinpoint.otlp.web.view;

import java.util.List;
import java.util.Map;

public class OtlpChartSummaryViewBuilder extends OtlpChartViewBuilder {


    private static final String FIELD_KEYWORD_COUNT = "count";
    private static final String FIELD_KEYWORD_SUM = "sum";
    private static final String METADATA_KEYWORD_NUMQUANTILES = "numQuantiles";

    private List<Number> numQuantiles;
    //private Map<String, String> quantileMap; // ?? it can change in time

    protected OtlpChartSummaryViewBuilder() {
        super(CHART_TYPE_AREA);
    }

    @Override
    protected String checkChartType(String fieldName, String description) {
        if (fieldName.equals(FIELD_KEYWORD_COUNT) || fieldName.equals(FIELD_KEYWORD_SUM)) {
            return CHART_TYPE_SPLINE;
        } else if (fieldName.equals(METADATA_KEYWORD_NUMQUANTILES)) {
            return CHART_TYPE_NONE;
        } else {
            return CHART_TYPE_AREA;
        }
    }

    @Override
    protected void setMetadata(String name, List<Number> values, String description) {
        if (name.equals(METADATA_KEYWORD_NUMQUANTILES)) {
            this.numQuantiles = values;
            this.description = description;
        } else {
            throw new OtlpParsingException("Invalid metadata name: " + name);
        }

    }
}

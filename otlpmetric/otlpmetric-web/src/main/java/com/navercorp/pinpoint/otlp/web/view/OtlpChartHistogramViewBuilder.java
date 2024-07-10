package com.navercorp.pinpoint.otlp.web.view;

import java.util.List;

public class OtlpChartHistogramViewBuilder extends OtlpChartViewBuilder {
    private static final String FIELD_KEYWORD_COUNT = "count";
    private static final String FIELD_KEYWORD_SUM = "sum";
    private static final String FIELD_KEYWORD_MAX = "max";
    private static final String FIELD_KEYWORD_MIN = "min";
    private static final String METADATA_KEYWORD_NUMBUCKETS= "numBuckets";

    private long numBuckets;

    protected OtlpChartHistogramViewBuilder() {
        super(CHART_TYPE_BAR);
    }

    @Override
    protected String checkChartType(String fieldName, String description) {
        switch (fieldName) {
            case FIELD_KEYWORD_COUNT -> {
                this.description = description;
                return CHART_TYPE_SPLINE;
            }
            case FIELD_KEYWORD_SUM, FIELD_KEYWORD_MAX, FIELD_KEYWORD_MIN -> {
                return CHART_TYPE_SPLINE;
            }
            case METADATA_KEYWORD_NUMBUCKETS -> {
                return CHART_TYPE_NONE;
            }
            default -> {
                return CHART_TYPE_BAR;
            }
        }
    }

    @Override
    public void setMetadata(String name, List<Number> values, String description) {
        if (name.equals(METADATA_KEYWORD_NUMBUCKETS)) {
            int lastIndex = values.size() - 1;
            this.numBuckets = values.get(lastIndex).longValue();
            this.description = description;
        } else {
            throw new OtlpParsingException("Invalid metadata name: " + name);
        }
    }
}

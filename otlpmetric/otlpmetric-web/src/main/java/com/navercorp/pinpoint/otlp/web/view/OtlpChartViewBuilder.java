package com.navercorp.pinpoint.otlp.web.view;

import com.navercorp.pinpoint.otlp.common.model.MetricType;
import com.navercorp.pinpoint.otlp.web.vo.FieldAttribute;
import com.navercorp.pinpoint.otlp.web.vo.OtlpMetricChartResult;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public abstract class OtlpChartViewBuilder {
    protected static final String CHART_TYPE_SPLINE = "spline";
    protected static final String CHART_TYPE_BAR = "bar";
    protected static final String CHART_TYPE_AREA = "area-spline";
    protected static final String CHART_TYPE_NONE = "none";

    protected boolean hasSummaryField = false;
    final String defaultChartType;

    private List<Long> timestamp;
    protected String description;
    protected List<OtlpChartFieldView> fields = new ArrayList<>();

    protected final OtlpChartFieldViewBuilder chartFieldViewBuilder;

    public static OtlpChartView EMPTY_CHART_VIEW = new OtlpChartView(new ArrayList<>(), "", false, new ArrayList<>());

    protected OtlpChartViewBuilder(String defaultChartType) {
        this.defaultChartType = defaultChartType;
        this.chartFieldViewBuilder = new OtlpChartFieldViewBuilder();
    }

    public static OtlpChartViewBuilder newBuilder(MetricType chartType) {
        switch (chartType) {
            case SUM:
            case GAUGE:
                return new OtlpChartSumGaugeViewBuilder();
            case HISTOGRAM:
                return new OtlpChartHistogramViewBuilder();
            case EXP_HISTOGRAM:
                return new OtlpChartExpHistogramViewBuilder();
            case SUMMARY:
                return new OtlpChartSummaryViewBuilder();
            default:
                throw new OtlpParsingException("Should not reach here.");
        }
    }

    public OtlpChartView build() {
        checkValidity(timestamp.size());
        return new OtlpChartView(timestamp, description, hasSummaryField, fields);
    }

    public OtlpChartViewBuilder self() {
        return this;
    }

    public OtlpChartViewBuilder setTimestamp(List<Long> timestamp) {
        this.timestamp = timestamp;
        return self();
    }

    public List<Long> getTimestamp() {
        return timestamp;
    }

    public List<OtlpChartFieldView> getFields() {
        return fields;
    }

    protected void checkValidity(int timestampSize) {
        fields.forEach(field -> {
            if (field.getValues().size() != timestampSize) {
                throw new OtlpParsingException("Invalid field values size: " + field.getValues().size() + " with timestamp size: " + timestampSize);
            }
        });
    }

    protected abstract String checkChartType(String fieldName, String description);

    protected abstract void setMetadata(String name, List<Number> values, String description);

    public OtlpChartFieldView add(FieldAttribute field, List<OtlpMetricChartResult> dataPoints) {
        String fieldName = field.fieldName();
        String chartType = checkChartType(fieldName, field.description());
        List<Number> value = dataPoints.stream().map(OtlpMetricChartResult::value).collect(Collectors.toList());

        if (chartType.equals(CHART_TYPE_NONE)) {
            setMetadata(fieldName, value, field.description());
            return null;
        }

        List<Long> timestamp = dataPoints.stream().map(OtlpMetricChartResult::eventTime).collect(Collectors.toList());

        OtlpChartFieldView chartFieldView;
        if (this.timestamp == null) {
            this.timestamp = timestamp;
            chartFieldViewBuilder.setChartType(chartType)
                    .setFieldName(fieldName).setDescription(field.description()).setUnit(field.unit()).setVersion(field.version())
                    .setAggregationTemporality(field.aggregationTemporality().name()).setValues(value);
            chartFieldView = chartFieldViewBuilder.build();
        } else {
            chartFieldView = OtlpChartFieldViewBuilder.makeFilledFieldData(chartType, field, this, timestamp, value);
        }
        fields.add(chartFieldView);
        return chartFieldView;
    }

    public void shiftFillEmptyValue(int index, long timestamp) {
        this.timestamp.add(index, timestamp);
        for (OtlpChartFieldView chartFieldView : fields) {
            List<Number> values = chartFieldView.getValues();
            values.add(index, -1);
        }
    }

    public boolean hasSummaryField() {
        return hasSummaryField;
    }
}

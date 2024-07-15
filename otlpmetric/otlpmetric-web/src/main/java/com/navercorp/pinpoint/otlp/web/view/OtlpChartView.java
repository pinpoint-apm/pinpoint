package com.navercorp.pinpoint.otlp.web.view;

import com.navercorp.pinpoint.otlp.common.model.MetricType;
import com.navercorp.pinpoint.otlp.web.vo.FieldAttribute;
import com.navercorp.pinpoint.otlp.web.vo.OtlpMetricChartResult;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class OtlpChartView {
    private List<Long> timestamp;
    private String description;
    private boolean hasSummary = false;
    private List<OtlpChartFieldView> fields;
    //private OtlpChartFieldViewBuilder chartFieldViewBuilder;

    public OtlpChartView(List<Long> timestamp, String description, boolean hasSummary, List<OtlpChartFieldView> fields) {
        this.timestamp = timestamp;
        this.description = description;
        this.hasSummary = hasSummary;
        this.fields = fields;
    }

    public OtlpChartView(int chartType) {
        MetricType metricType = MetricType.forNumber(chartType);
        if (metricType == null) {
            throw new OtlpParsingException("Cannot get valid metric type for requested chart.");
        }
        fields = new ArrayList<>();
    }

    public List<Long> getTimestamp() {
        return timestamp;
    }

    public String getDescription() {
        return description;
    }

    public List<OtlpChartFieldView> getFields() {
        return fields;
    }

    /*
    private void checkValidity(MetricType chartType) {
        // make chart with above data
        switch (chartType) {
            case SUM:
            case GAUGE:
                this.chartFieldViewBuilder = new OtlpChartSumGaugeFieldViewBuilder();
                this.hasSummary = true;
                break;
            case HISTOGRAM:
                this.chartFieldViewBuilder = new OtlpChartHistogramFieldViewBuilder();
                break;
            case EXP_HISTOGRAM:
                //this.chartFieldViewBuilder = new OtlpChartHistogramFieldViewBuilder();
                break;
            case SUMMARY:
                //this.chartType = "area-spline";
                break;
            default:
                throw new OtlpParsingException("Should not reach here.");
        }
    }*/
}

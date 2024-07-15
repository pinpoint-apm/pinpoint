package com.navercorp.pinpoint.otlp.web.view;

import com.navercorp.pinpoint.otlp.common.model.AggreFunc;
import com.navercorp.pinpoint.otlp.common.model.AggreTemporality;
import com.navercorp.pinpoint.otlp.common.model.DataType;
import com.navercorp.pinpoint.otlp.common.model.MetricType;
import com.navercorp.pinpoint.otlp.web.vo.FieldAttribute;
import com.navercorp.pinpoint.otlp.web.vo.OtlpMetricChartResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class OtlpChartFieldViewTest {
    private String chartType = "spline";
    private FieldAttribute testField = new FieldAttribute("fieldName", MetricType.GAUGE, DataType.DOUBLE, AggreFunc.AVERAGE, AggreTemporality.UNSPECIFIED, "description", "unit", "version");
    private OtlpMetricChartResult dp1 = new OtlpMetricChartResult(1L, "", 1);
    private OtlpMetricChartResult dp2 = new OtlpMetricChartResult(3L, "", 2);
    private OtlpMetricChartResult dp3 = new OtlpMetricChartResult(4L, "", 3);
    private OtlpMetricChartResult dp4 = new OtlpMetricChartResult(5L, "", 4);
    private OtlpMetricChartResult dp5 = new OtlpMetricChartResult(6L, "", 5);

    private OtlpChartViewBuilder parentViewBuilder;

    @BeforeEach
    public void init() {
        List<OtlpMetricChartResult> dataPoints1 = Arrays.asList(dp1, dp2, dp3, dp4, dp5);
        parentViewBuilder = OtlpChartViewBuilder.newBuilder(MetricType.GAUGE);
        parentViewBuilder.add(testField, dataPoints1);
    }

    @Test
    public void fillParentWhenParentStartsLate() {
        List<Long> timestamp = new ArrayList<>(Arrays.asList(0L, 1L, 3L, 4L, 5L, 6L));
        List<Number> value = new ArrayList<>(Arrays.asList(1, 2, 3, 4, 5, 6));

        assertThat(parentViewBuilder.getTimestamp().get(0)).isEqualTo(1L);
        OtlpChartFieldViewBuilder.makeFilledFieldData(chartType, testField, parentViewBuilder, timestamp, value);
        assertThat(parentViewBuilder.getTimestamp().get(0)).isEqualTo(0L);
    }

    @Test
    public void fillChildWhenChildStartsLate() {
        List<Long> timestamp = new ArrayList<>(Arrays.asList(3L, 4L, 5L, 6L));
        List<Number> value = new ArrayList<>(Arrays.asList(1, 2, 3, 4));

        assertThat(parentViewBuilder.getTimestamp().get(0)).isEqualTo(1L);
        OtlpChartFieldView result = OtlpChartFieldViewBuilder.makeFilledFieldData(chartType, testField, parentViewBuilder, timestamp, value);
        assertThat(parentViewBuilder.getTimestamp().get(0)).isEqualTo(1L);
        assertThat(result.getValues().get(0)).isEqualTo(-1);
    }

    @Test
    public void fillParentTimestampTail() {
        List<Long> currentTimestamp = new ArrayList<>(Arrays.asList(1L, 3L, 4L, 5L, 6L, 7L));
        List<Number> value = new ArrayList<>(Arrays.asList(1, 2, 3, 4, 5, 6));

        assertThat(parentViewBuilder.getTimestamp().size()).isEqualTo(5);
        OtlpChartFieldViewBuilder.makeFilledFieldData(chartType, testField, parentViewBuilder, currentTimestamp, value);
        assertThat(parentViewBuilder.getTimestamp().size()).isEqualTo(6);
        assertThat(parentViewBuilder.getTimestamp().get(5)).isEqualTo(7L);
    }

    @Test
    public void fillChildValueTail() {
        List<Long> currentTimestamp = new ArrayList<>(Arrays.asList(1L, 3L, 4L, 5L));
        List<Number> value = new ArrayList<>(Arrays.asList(1, 2, 3, 4));

        OtlpChartFieldView result = OtlpChartFieldViewBuilder.makeFilledFieldData(chartType, testField, parentViewBuilder, currentTimestamp, value);
        assertThat(result.getValues().size()).isEqualTo(5);
        assertThat(result.getValues().get(4)).isEqualTo(-1);
    }

    @Test
    public void fillParentValueMiddle() {
        List<Long> currentTimestamp = new ArrayList<>(Arrays.asList(1L, 2L, 3L, 4L, 5L, 6L));
        List<Number> value = new ArrayList<>(Arrays.asList(1, 2, 3, 4, 5, 6));

        assertThat(parentViewBuilder.getTimestamp().size()).isEqualTo(5);
        OtlpChartFieldViewBuilder.makeFilledFieldData(chartType, testField, parentViewBuilder, currentTimestamp, value);
        assertThat(parentViewBuilder.getTimestamp().size()).isEqualTo(6);
        assertThat(parentViewBuilder.getTimestamp().get(1)).isEqualTo(2L);
    }

    @Test
    public void fillChildValueMiddle() {
        List<Long> currentTimestamp = new ArrayList<>(Arrays.asList(1L, 3L, 6L));
        List<Number> value = new ArrayList<>(Arrays.asList(1, 2, 3));

        OtlpChartFieldView result = OtlpChartFieldViewBuilder.makeFilledFieldData(chartType, testField, parentViewBuilder, currentTimestamp, value);
        assertThat(result.getValues().size()).isEqualTo(5);
        assertThat(result.getValues().get(2)).isEqualTo(-1);
        assertThat(result.getValues().get(3)).isEqualTo(-1);
    }

}

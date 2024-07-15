package com.navercorp.pinpoint.otlp.web.view;

import com.navercorp.pinpoint.otlp.common.model.AggreFunc;
import com.navercorp.pinpoint.otlp.common.model.AggreTemporality;
import com.navercorp.pinpoint.otlp.common.model.DataType;
import com.navercorp.pinpoint.otlp.common.model.MetricType;
import com.navercorp.pinpoint.otlp.web.vo.FieldAttribute;
import com.navercorp.pinpoint.otlp.web.vo.OtlpMetricChartResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class OtlpChartViewTest {

    private OtlpChartViewBuilder otlpChartViewBuilder;

    @BeforeEach
    public void setup() {
        otlpChartViewBuilder = OtlpChartViewBuilder.newBuilder(MetricType.GAUGE);
    }

    @Test
    public void shiftFillEmptyValueShouldAddTimestampAndValue() {
        FieldAttribute fieldAttribute = new FieldAttribute("test", MetricType.GAUGE, DataType.DOUBLE, AggreFunc.AVERAGE, AggreTemporality.DELTA, "description", "unit", "version");
        List<OtlpMetricChartResult> dataPoints = Arrays.asList(new OtlpMetricChartResult(123456789L, "", 100));

        otlpChartViewBuilder.add(fieldAttribute, dataPoints);
        otlpChartViewBuilder.shiftFillEmptyValue(0, 123456789L);
        assertEquals(2, otlpChartViewBuilder.getTimestamp().size());
        assertEquals(123456789L, otlpChartViewBuilder.getTimestamp().get(1));
    }

    @Test
    public void setTimestampShouldSetTimestamp() {
        List<Long> timestamps = Arrays.asList(123456789L, 987654321L);
        otlpChartViewBuilder.setTimestamp(timestamps);
        assertEquals(timestamps, otlpChartViewBuilder.getTimestamp());
    }

    @Test
    public void addShouldAddFieldData() {
        FieldAttribute fieldAttribute = new FieldAttribute("test", MetricType.GAUGE, DataType.DOUBLE, AggreFunc.AVERAGE, AggreTemporality.DELTA, "description", "unit", "version");
        List<OtlpMetricChartResult> dataPoints1 = Arrays.asList(new OtlpMetricChartResult(123456789L, "", 100));
        otlpChartViewBuilder.add(fieldAttribute, dataPoints1);
        assertEquals(1, otlpChartViewBuilder.getFields().size());

        List<OtlpMetricChartResult> dataPoints2 = Arrays.asList(new OtlpMetricChartResult(123456789L, "", 100));
        otlpChartViewBuilder.add(fieldAttribute, dataPoints2);
        assertEquals(2, otlpChartViewBuilder.getFields().size());
    }

    @Test
    public void checkValidityShouldThrowExceptionForInvalidMetricType() {
        assertThrows(OtlpParsingException.class, () -> new OtlpChartView(-1));
    }
}
package com.navercorp.pinpoint.inspector.web.definition.metric;

import com.navercorp.pinpoint.inspector.web.model.InspectorMetricValue;
import com.navercorp.pinpoint.metric.web.model.MetricValue;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author minwoo.jung
 */
class AvgUsingIntervalPostProcessorTest {

    @Test
    public void postProcessTest() {
        AvgUsingIntervalPostProcessor avgUsingIntervalPostProcessor = new AvgUsingIntervalPostProcessor();
        List<InspectorMetricValue> metricValueList = new ArrayList<>();

        List<Double> sampledNewCountValueList = List.of(
                5.0, 10.0, 15.0, 20.0, 25.0
        );
        InspectorMetricValue sampledNewCount = new InspectorMetricValue("sampledNewCount", List.of(), "splineChart", "count", sampledNewCountValueList);
        metricValueList.add(sampledNewCount);

        List<Double> sampledContinuationCountValueList = List.of(
                10.0, 15.0, 20.0, 25.0, 30.0
        );
        InspectorMetricValue sampledContinuationCount = new InspectorMetricValue("sampledContinuationCount", List.of(), "splineChart", "count", sampledContinuationCountValueList);
        metricValueList.add(sampledContinuationCount);

        List<Double> unsampledNewCountValueList = List.of(
                15.0, 20.0, 25.0, 30.0, 35.0
        );
        InspectorMetricValue unsampledNewCount = new InspectorMetricValue("unsampledNewCount", List.of(), "splineChart", "count", unsampledNewCountValueList);
        metricValueList.add(unsampledNewCount);

        List<Double> unsampledContinuationCountValueList = List.of(
                20.0, 25.0, 30.0, 35.0, 40.0
        );
        InspectorMetricValue unsampledContinuationCount = new InspectorMetricValue("unsampledContinuationCount", List.of(), "splineChart", "count", unsampledContinuationCountValueList);
        metricValueList.add(unsampledContinuationCount);

        List<Double> skippedNewSkipCountValueList = List.of(
                25.0, 30.0, 35.0, 40.0, 45.0
        );
        InspectorMetricValue skippedNewSkipCount = new InspectorMetricValue("skippedNewSkipCount", List.of(), "splineChart", "count", skippedNewSkipCountValueList);
        metricValueList.add(skippedNewSkipCount);

        List<Double> skippedContinuationCountValueList = List.of(
                30.0, 35.0, 40.0, 45.0, 50.0
        );
        InspectorMetricValue skippedContinuationCount = new InspectorMetricValue("skippedContinuationCount", List.of(), "splineChart", "count", skippedContinuationCountValueList);
        metricValueList.add(skippedContinuationCount);

        List<Double> collectIntervalValueList = List.of(
                5000.0, 4000.0, 3000.0, 2000.0, 1000.0
        );
        InspectorMetricValue collectInterval = new InspectorMetricValue("collectInterval", List.of(), "splineChart", "count", collectIntervalValueList);
        metricValueList.add(collectInterval);

        List<InspectorMetricValue> processedValuesList = avgUsingIntervalPostProcessor.postProcess(metricValueList);
        Assertions.assertThat(processedValuesList)
                .hasSize(7);


        Map<String, InspectorMetricValue> processedValuesMap = processedValuesList
                .stream()
                .collect(Collectors.toMap(MetricValue::getFieldName, Function.identity()));

        MetricValue<Double> processedSampledNewCount = processedValuesMap.get("sampledNewCount");
        Assertions.assertThat(processedSampledNewCount.getValueList())
                .hasSize(5)
                .containsExactly(1.0, 2.5, 5.0, 10.0, 25.0);


        MetricValue<Double> processedSampledContinuationCount = processedValuesMap.get("sampledContinuationCount");
        Assertions.assertThat(processedSampledContinuationCount.getValueList())
                .hasSize(5)
                .containsExactly(2.0, 3.8, 6.7, 12.5, 30.0);


        MetricValue<Double> processedTotalCount = processedValuesMap.get("totalCount");
        Assertions.assertThat(processedTotalCount.getValueList())
                .hasSize(5)
                .containsExactly(21.0, 33.9, 55.0, 97.5, 225.0);
    }

}
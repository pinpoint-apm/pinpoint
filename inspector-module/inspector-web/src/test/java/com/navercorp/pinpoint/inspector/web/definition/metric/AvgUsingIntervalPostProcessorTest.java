package com.navercorp.pinpoint.inspector.web.definition.metric;

import com.navercorp.pinpoint.inspector.web.model.InspectorMetricValue;
import com.navercorp.pinpoint.metric.web.model.MetricValue;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author minwoo.jung
 */
class AvgUsingIntervalPostProcessorTest {

    @Test
    public void postProcessTest() {
        AvgUsingIntervalPostProcessor avgUsingIntervalPostProcessor = new AvgUsingIntervalPostProcessor();
        List<InspectorMetricValue> metricValueList = new ArrayList<>();

        List<Double> sampledNewCountValueList = new ArrayList<>();
        sampledNewCountValueList.add(5.0);
        sampledNewCountValueList.add(10.0);
        sampledNewCountValueList.add(15.0);
        sampledNewCountValueList.add(20.0);
        sampledNewCountValueList.add(25.0);
        InspectorMetricValue sampledNewCount = new InspectorMetricValue("sampledNewCount", Collections.emptyList(), "splineChart", "count", sampledNewCountValueList);
        metricValueList.add(sampledNewCount);
        
        List<Double> sampledContinuationCountValueList = new ArrayList<>();
        sampledContinuationCountValueList.add(10.0);
        sampledContinuationCountValueList.add(15.0);
        sampledContinuationCountValueList.add(20.0);
        sampledContinuationCountValueList.add(25.0);
        sampledContinuationCountValueList.add(30.0);
        InspectorMetricValue sampledContinuationCount = new InspectorMetricValue("sampledContinuationCount", Collections.emptyList(), "splineChart", "count", sampledContinuationCountValueList);
        metricValueList.add(sampledContinuationCount);
        
        List<Double> unsampledNewCountValueList = new ArrayList<>();
        unsampledNewCountValueList.add(15.0);
        unsampledNewCountValueList.add(20.0);
        unsampledNewCountValueList.add(25.0);
        unsampledNewCountValueList.add(30.0);
        unsampledNewCountValueList.add(35.0);
        InspectorMetricValue unsampledNewCount = new InspectorMetricValue("unsampledNewCount", Collections.emptyList(), "splineChart", "count", unsampledNewCountValueList);
        metricValueList.add(unsampledNewCount);
        
        List<Double> unsampledContinuationCountValueList = new ArrayList<>();
        unsampledContinuationCountValueList.add(20.0);
        unsampledContinuationCountValueList.add(25.0);
        unsampledContinuationCountValueList.add(30.0);
        unsampledContinuationCountValueList.add(35.0);
        unsampledContinuationCountValueList.add(40.0);
        InspectorMetricValue unsampledContinuationCount = new InspectorMetricValue("unsampledContinuationCount", Collections.emptyList(), "splineChart", "count", unsampledContinuationCountValueList);
        metricValueList.add(unsampledContinuationCount);
        
        List<Double> skippedNewSkipCountValueList = new ArrayList<>();
        skippedNewSkipCountValueList.add(25.0);
        skippedNewSkipCountValueList.add(30.0);
        skippedNewSkipCountValueList.add(35.0);
        skippedNewSkipCountValueList.add(40.0);
        skippedNewSkipCountValueList.add(45.0);
        InspectorMetricValue skippedNewSkipCount = new InspectorMetricValue("skippedNewSkipCount", Collections.emptyList(), "splineChart", "count", skippedNewSkipCountValueList);
        metricValueList.add(skippedNewSkipCount);
        
        List<Double> skippedContinuationCountValueList = new ArrayList<>();
        skippedContinuationCountValueList.add(30.0);
        skippedContinuationCountValueList.add(35.0);
        skippedContinuationCountValueList.add(40.0);
        skippedContinuationCountValueList.add(45.0);
        skippedContinuationCountValueList.add(50.0);
        InspectorMetricValue skippedContinuationCount = new InspectorMetricValue("skippedContinuationCount", Collections.emptyList(), "splineChart", "count", skippedContinuationCountValueList);
        metricValueList.add(skippedContinuationCount);
        
        List<Double> collectIntervalValueList = new ArrayList<>();
        collectIntervalValueList.add(5000.0);
        collectIntervalValueList.add(4000.0);
        collectIntervalValueList.add(3000.0);
        collectIntervalValueList.add(2000.0);
        collectIntervalValueList.add(1000.0);
        InspectorMetricValue collectInterval = new InspectorMetricValue("collectInterval", Collections.emptyList(), "splineChart", "count", collectIntervalValueList);
        metricValueList.add(collectInterval);

        List<InspectorMetricValue> processedValuesList = avgUsingIntervalPostProcessor.postProcess(metricValueList);
        assertEquals(7, processedValuesList.size());


        Map<String, InspectorMetricValue> processedValuesMap = processedValuesList.stream().collect(Collectors.toMap(MetricValue::getFieldName, Function.identity()));

        MetricValue<Double> processedSampledNewCount = processedValuesMap.get("sampledNewCount");
        assertEquals(5,processedSampledNewCount.getValueList().size());

        assertEquals(1.0, processedSampledNewCount.getValueList().get(0));
        assertEquals(2.5, processedSampledNewCount.getValueList().get(1));
        assertEquals(5.0, processedSampledNewCount.getValueList().get(2));
        assertEquals(10.0, processedSampledNewCount.getValueList().get(3));
        assertEquals(25.0, processedSampledNewCount.getValueList().get(4));

        MetricValue<Double> processedSampledContinuationCount = processedValuesMap.get("sampledContinuationCount");
        assertEquals(5,processedSampledContinuationCount.getValueList().size());

        assertEquals(2.0, processedSampledContinuationCount.getValueList().get(0));
        assertEquals(3.8, processedSampledContinuationCount.getValueList().get(1));
        assertEquals(6.7, processedSampledContinuationCount.getValueList().get(2));
        assertEquals(12.5, processedSampledContinuationCount.getValueList().get(3));
        assertEquals(30.0, processedSampledContinuationCount.getValueList().get(4));

        MetricValue<Double> processedTotalCount = processedValuesMap.get("totalCount");
        assertEquals(5,processedTotalCount.getValueList().size());

        assertEquals(21.0, processedTotalCount.getValueList().get(0));
        assertEquals(33.9, processedTotalCount.getValueList().get(1));
        assertEquals(55.0, processedTotalCount.getValueList().get(2));
        assertEquals(97.5, processedTotalCount.getValueList().get(3));
        assertEquals(225.0, processedTotalCount.getValueList().get(4));
    }

}
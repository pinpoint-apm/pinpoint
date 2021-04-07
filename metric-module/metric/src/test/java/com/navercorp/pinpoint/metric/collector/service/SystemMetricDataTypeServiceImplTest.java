package com.navercorp.pinpoint.metric.collector.service;

import com.navercorp.pinpoint.metric.common.model.*;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.ArrayList;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * @author minwoo.jung
 */
@RunWith(MockitoJUnitRunner.class)
public class SystemMetricDataTypeServiceImplTest {

    @Test
    public void saveMetricDataTypeTest() {
        MetricDataTypeCache metricDataTypeCache = mock(MetricDataTypeCache.class);
        SystemMetricDataTypeService systemMetricDataTypeService = new SystemMetricDataTypeServiceImpl(metricDataTypeCache);
        MetricData metricData = new MetricData("metricName", "fieldName", MetricDataType.DOUBLE);
        when(metricDataTypeCache.getMetricDataType(any(MetricDataName.class))).thenReturn(metricData);

        SystemMetric systemMetric = new DoubleMetric("metricName", "hostName", "fieldName", 0, new ArrayList<>(), Long.MAX_VALUE);
        systemMetricDataTypeService.saveMetricDataType(systemMetric);

        verify(metricDataTypeCache, never()).saveMetricDataType(any(MetricDataName.class), any(MetricData.class));
    }

    @Test
    public void saveMetricDataType2Test() {
        MetricDataTypeCache metricDataTypeCache = mock(MetricDataTypeCache.class);
        SystemMetricDataTypeService systemMetricDataTypeService = new SystemMetricDataTypeServiceImpl(metricDataTypeCache);
        when(metricDataTypeCache.getMetricDataType(any(MetricDataName.class))).thenReturn(null);

        SystemMetric systemMetric = new DoubleMetric("metricName", "hostName", "fieldName", 0, new ArrayList<>(), Long.MAX_VALUE);
        systemMetricDataTypeService.saveMetricDataType(systemMetric);

        MetricDataName metricDataName = new MetricDataName(systemMetric.getMetricName(), systemMetric.getFieldName());
        MetricData metricData = new MetricData("metricName", "fieldName", MetricDataType.DOUBLE);
        verify(metricDataTypeCache, times(1)).saveMetricDataType(metricDataName, metricData);
    }

    @Test
    public void saveMetricDataType3Test() {
        MetricDataTypeCache metricDataTypeCache = mock(MetricDataTypeCache.class);
        SystemMetricDataTypeService systemMetricDataTypeService = new SystemMetricDataTypeServiceImpl(metricDataTypeCache);
        when(metricDataTypeCache.getMetricDataType(any(MetricDataName.class))).thenReturn(null);

        SystemMetric systemMetric = new LongMetric("metricName", "hostName", "fieldName", 0, new ArrayList<>(), Long.MAX_VALUE);
        systemMetricDataTypeService.saveMetricDataType(systemMetric);

        MetricDataName metricDataName = new MetricDataName(systemMetric.getMetricName(), systemMetric.getFieldName());
        MetricData metricData = new MetricData("metricName", "fieldName", MetricDataType.LONG);
        verify(metricDataTypeCache, times(1)).saveMetricDataType(metricDataName, metricData);
    }

    @Test
    public void saveMetricDataType4Test() {
        MetricDataTypeCache metricDataTypeCache = mock(MetricDataTypeCache.class);
        SystemMetricDataTypeService systemMetricDataTypeService = new SystemMetricDataTypeServiceImpl(metricDataTypeCache);
        when(metricDataTypeCache.getMetricDataType(any(MetricDataName.class))).thenReturn(null);

        SystemMetric systemMetric = new SystemMetric("metricName", "fieldName", "hostName", new ArrayList<>(), Long.MAX_VALUE);
        systemMetricDataTypeService.saveMetricDataType(systemMetric);

        MetricDataName metricDataName = new MetricDataName(systemMetric.getMetricName(), systemMetric.getFieldName());
        MetricData metricData = new MetricData("metricName", "fieldName", MetricDataType.LONG);
        verify(metricDataTypeCache, times(0)).saveMetricDataType(any(MetricDataName.class), any(MetricData.class));
    }


}
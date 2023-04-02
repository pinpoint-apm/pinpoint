package com.navercorp.pinpoint.metric.collector.service;

import com.navercorp.pinpoint.metric.common.model.DoubleMetric;
import com.navercorp.pinpoint.metric.common.model.MetricData;
import com.navercorp.pinpoint.metric.common.model.MetricDataName;
import com.navercorp.pinpoint.metric.common.model.MetricDataType;
import com.navercorp.pinpoint.metric.common.model.SystemMetric;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * @author minwoo.jung
 */
@ExtendWith(MockitoExtension.class)
public class SystemMetricDataTypeServiceImplTest {

    @Test
    public void saveMetricDataTypeTest() {
        MetricDataTypeCache metricDataTypeCache = mock(MetricDataTypeCache.class);
        SystemMetricDataTypeService systemMetricDataTypeService = new SystemMetricDataTypeServiceImpl(metricDataTypeCache);
        MetricDataName metricDataName = new MetricDataName("metricName", "fieldName");
        MetricData metricData = new MetricData("metricName", "fieldName", MetricDataType.DOUBLE, metricDataName.getSaveTime());
        when(metricDataTypeCache.getMetricDataType(any(MetricDataName.class))).thenReturn(metricData);

        SystemMetric systemMetric = new DoubleMetric("metricName", "hostName", "fieldName", 0, List.of(), Long.MAX_VALUE);
        systemMetricDataTypeService.saveMetricDataType(systemMetric);

        verify(metricDataTypeCache, never()).saveMetricDataType(any(MetricDataName.class), any(MetricData.class));
    }

    @Test
    public void saveMetricDataType2Test() {
        MetricDataTypeCache metricDataTypeCache = mock(MetricDataTypeCache.class);
        SystemMetricDataTypeService systemMetricDataTypeService = new SystemMetricDataTypeServiceImpl(metricDataTypeCache);
        when(metricDataTypeCache.getMetricDataType(any(MetricDataName.class))).thenReturn(null);

        SystemMetric systemMetric = new DoubleMetric("metricName", "hostName", "fieldName", 0, List.of(), Long.MAX_VALUE);
        systemMetricDataTypeService.saveMetricDataType(systemMetric);

        MetricDataName metricDataName = new MetricDataName(systemMetric.getMetricName(), systemMetric.getFieldName());
        MetricData metricData = new MetricData("metricName", "fieldName", MetricDataType.DOUBLE, metricDataName.getSaveTime());
        verify(metricDataTypeCache).saveMetricDataType(metricDataName, metricData);
    }

    @Test
    public void saveMetricDataType3Test() {
        MetricDataTypeCache metricDataTypeCache = mock(MetricDataTypeCache.class);
        SystemMetricDataTypeService systemMetricDataTypeService = new SystemMetricDataTypeServiceImpl(metricDataTypeCache);
        when(metricDataTypeCache.getMetricDataType(any(MetricDataName.class))).thenReturn(null);

        SystemMetric systemMetric = new DoubleMetric("metricName", "hostName", "fieldName", 0, List.of(), Long.MAX_VALUE);
        systemMetricDataTypeService.saveMetricDataType(systemMetric);

        MetricDataName metricDataName = new MetricDataName(systemMetric.getMetricName(), systemMetric.getFieldName());
        MetricData metricData = new MetricData("metricName", "fieldName", MetricDataType.DOUBLE, metricDataName.getSaveTime());
        verify(metricDataTypeCache).saveMetricDataType(metricDataName, metricData);
    }

    @Test
    public void saveMetricDataType4Test() {
        MetricDataTypeCache metricDataTypeCache = mock(MetricDataTypeCache.class);
        SystemMetricDataTypeService systemMetricDataTypeService = new SystemMetricDataTypeServiceImpl(metricDataTypeCache);
        when(metricDataTypeCache.getMetricDataType(any(MetricDataName.class))).thenReturn(null);

        SystemMetric systemMetric = new SystemMetric("metricName", "fieldName", "hostName", List.of(), Long.MAX_VALUE);
        systemMetricDataTypeService.saveMetricDataType(systemMetric);

        MetricDataName metricDataName = new MetricDataName(systemMetric.getMetricName(), systemMetric.getFieldName());
        MetricData metricData = new MetricData("metricName", "fieldName", MetricDataType.LONG, metricDataName.getSaveTime());
        verify(metricDataTypeCache, never()).saveMetricDataType(any(MetricDataName.class), any(MetricData.class));
    }
}
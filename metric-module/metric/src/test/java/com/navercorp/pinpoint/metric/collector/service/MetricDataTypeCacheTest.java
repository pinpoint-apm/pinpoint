package com.navercorp.pinpoint.metric.collector.service;

import com.navercorp.pinpoint.metric.collector.dao.SystemMetricDataTypeDao;
import com.navercorp.pinpoint.metric.common.model.MetricData;
import com.navercorp.pinpoint.metric.common.model.MetricDataName;
import com.navercorp.pinpoint.metric.common.model.MetricDataType;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @author minwoo.jung
 */
@ExtendWith(MockitoExtension.class)
public class MetricDataTypeCacheTest {

    @Test
    public void getMetricDataTypeTest() {
        SystemMetricDataTypeDao systemMetricDataTypeDao = mock(SystemMetricDataTypeDao.class);
        MetricDataTypeCache metricDataTypeCache = new MetricDataTypeCache(systemMetricDataTypeDao);
        MetricData metricData = new MetricData("metricName", "fieldName", MetricDataType.DOUBLE);

        MetricDataName metricDataName = new MetricDataName(metricData.getMetricName(), metricData.getFieldName());
        when(systemMetricDataTypeDao.selectMetricDataType(metricDataName)).thenReturn(null);
        MetricData metricDataResult = metricDataTypeCache.getMetricDataType(metricDataName);

        Assertions.assertNull(metricDataResult);
    }

    @Test
    public void getMetricDataType2Test() {
        SystemMetricDataTypeDao systemMetricDataTypeDao = mock(SystemMetricDataTypeDao.class);
        MetricDataTypeCache metricDataTypeCache = new MetricDataTypeCache(systemMetricDataTypeDao);
        MetricData metricData = new MetricData("metricName", "fieldName", MetricDataType.DOUBLE);

        MetricDataName metricDataName = new MetricDataName(metricData.getMetricName(), metricData.getFieldName());
        when(systemMetricDataTypeDao.selectMetricDataType(metricDataName)).thenReturn(metricData);
        MetricData metricDataResult = metricDataTypeCache.getMetricDataType(metricDataName);

        Assertions.assertEquals(metricData, metricDataResult);
    }

    @Test
    public void saveMetricDataTypeTest() {
        SystemMetricDataTypeDao systemMetricDataTypeDao = mock(SystemMetricDataTypeDao.class);
        MetricDataTypeCache metricDataTypeCache = new MetricDataTypeCache(systemMetricDataTypeDao);
        MetricData metricData = new MetricData("metricName", "fieldName", MetricDataType.DOUBLE);

        MetricDataName metricDataName = new MetricDataName(metricData.getMetricName(), metricData.getFieldName());
        MetricData metricDataResult = metricDataTypeCache.saveMetricDataType(metricDataName, metricData);

        Assertions.assertEquals(metricData, metricDataResult);
    }
}
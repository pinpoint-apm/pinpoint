package com.navercorp.pinpoint.metric.collector.service;

import com.navercorp.pinpoint.metric.collector.dao.SystemMetricDataTypeDao;
import com.navercorp.pinpoint.metric.common.model.MetricData;
import com.navercorp.pinpoint.metric.common.model.MetricDataName;
import com.navercorp.pinpoint.metric.common.model.MetricDataType;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @author minwoo.jung
 */
@RunWith(MockitoJUnitRunner.class)
public class MetricDataTypeCacheTest {

    @Test
    public void getMetricDataTypeTest() {
        SystemMetricDataTypeDao systemMetricDataTypeDao = mock(SystemMetricDataTypeDao.class);
        MetricDataTypeCache metricDataTypeCache = new MetricDataTypeCache(systemMetricDataTypeDao);
        MetricData metricData = new MetricData("metricName", "fieldName", MetricDataType.DOUBLE);

        MetricDataName metricDataName = new MetricDataName(metricData.getMetricName(), metricData.getFieldName());
        when(systemMetricDataTypeDao.selectMetricDataType(metricDataName)).thenReturn(null);
        MetricData metricDataResult = metricDataTypeCache.getMetricDataType(metricDataName);

        Assert.assertNull(metricDataResult);
    }

    @Test
    public void getMetricDataType2Test() {
        SystemMetricDataTypeDao systemMetricDataTypeDao = mock(SystemMetricDataTypeDao.class);
        MetricDataTypeCache metricDataTypeCache = new MetricDataTypeCache(systemMetricDataTypeDao);
        MetricData metricData = new MetricData("metricName", "fieldName", MetricDataType.DOUBLE);

        MetricDataName metricDataName = new MetricDataName(metricData.getMetricName(), metricData.getFieldName());
        when(systemMetricDataTypeDao.selectMetricDataType(metricDataName)).thenReturn(metricData);
        MetricData metricDataResult = metricDataTypeCache.getMetricDataType(metricDataName);

        Assert.assertEquals(metricData, metricDataResult);
    }

    @Test
    public void saveMetricDataTypeTest() {
        SystemMetricDataTypeDao systemMetricDataTypeDao = mock(SystemMetricDataTypeDao.class);
        MetricDataTypeCache metricDataTypeCache = new MetricDataTypeCache(systemMetricDataTypeDao);
        MetricData metricData = new MetricData("metricName", "fieldName", MetricDataType.DOUBLE);

        MetricDataName metricDataName = new MetricDataName(metricData.getMetricName(), metricData.getFieldName());
        MetricData metricDataResult = metricDataTypeCache.saveMetricDataType(metricDataName, metricData);

        Assert.assertEquals(metricData, metricDataResult);
    }
}
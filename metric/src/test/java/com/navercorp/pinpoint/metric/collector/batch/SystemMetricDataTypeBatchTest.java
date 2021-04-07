package com.navercorp.pinpoint.metric.collector.batch;

import com.navercorp.pinpoint.metric.collector.dao.SystemMetricDataTypeDao;
import com.navercorp.pinpoint.metric.common.model.*;
import com.navercorp.pinpoint.metric.collector.service.SystemMetricDataTypeService;
import com.navercorp.pinpoint.metric.collector.service.SystemMetricDataTypeServiceImpl;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

/**
 * @author minwoo.jung
 */

@RunWith(MockitoJUnitRunner.class)
public class SystemMetricDataTypeBatchTest {

    @Mock
    SystemMetricDataTypeDao systemMetricDataTypeDao;

    @Test
    public void updateTest() {
        SystemMetricDataTypeService systemMetricDataTypeService = new SystemMetricDataTypeServiceImpl(systemMetricDataTypeDao);

        systemMetricDataTypeService.saveMetricDataType(new LongCounter("A_metricName1", "hostName", "A_fieldName1", 0, new ArrayList<>(0), 0));
        systemMetricDataTypeService.saveMetricDataType(new LongCounter("A_metricName2", "hostName", "A_fieldName2", 0, new ArrayList<>(0), 0));
        systemMetricDataTypeService.saveMetricDataType(new LongCounter("A_metricName3", "hostName", "A_fieldName3", 0, new ArrayList<>(0), 0));
        systemMetricDataTypeService.saveMetricDataType(new LongCounter("A_metricName4", "hostName", "A_fieldName4", 0, new ArrayList<>(0), 0));
        systemMetricDataTypeService.saveMetricDataType(new LongCounter("A_metricName5", "hostName", "A_fieldName5", 0, new ArrayList<>(0), 0));
        systemMetricDataTypeService.saveMetricDataType(new LongCounter("B_metricName1", "hostName", "B_fieldName1", 0, new ArrayList<>(0), 0));
        systemMetricDataTypeService.saveMetricDataType(new LongCounter("B_metricName2", "hostName", "B_fieldName2", 0, new ArrayList<>(0), 0));
        systemMetricDataTypeService.saveMetricDataType(new LongCounter("B_metricName3", "hostName", "B_fieldName3", 0, new ArrayList<>(0), 0));
        systemMetricDataTypeService.saveMetricDataType(new LongCounter("B_metricName4", "hostName", "B_fieldName4", 0, new ArrayList<>(0), 0));
        systemMetricDataTypeService.saveMetricDataType(new LongCounter("B_metricName5", "hostName", "B_fieldName5", 0, new ArrayList<>(0), 0));

        List<MetricData> metricDataTypeList = new ArrayList<>();
        metricDataTypeList.add(new MetricData("A_metricName1","A_fieldName1", MetricDataType.LONG));
        metricDataTypeList.add(new MetricData("A_metricName2","A_fieldName2", MetricDataType.LONG));
        metricDataTypeList.add(new MetricData("A_metricName3","A_fieldName3", MetricDataType.LONG));
        metricDataTypeList.add(new MetricData("A_metricName4","A_fieldName4", MetricDataType.LONG));
        metricDataTypeList.add(new MetricData("A_metricName5","A_fieldName5", MetricDataType.LONG));
        metricDataTypeList.add(new MetricData("C_metricName1","C_fieldName1", MetricDataType.LONG));
        metricDataTypeList.add(new MetricData("C_metricName2","C_fieldName2", MetricDataType.LONG));
        metricDataTypeList.add(new MetricData("C_metricName3","C_fieldName3", MetricDataType.LONG));
        metricDataTypeList.add(new MetricData("C_metricName4","C_fieldName4", MetricDataType.LONG));
        metricDataTypeList.add(new MetricData("C_metricName5","C_fieldName5", MetricDataType.LONG));
        when(systemMetricDataTypeDao.selectMetricDataType()).thenReturn(metricDataTypeList);

        SystemMetricDataTypeBatch systemMetricDataTypeBatch = new SystemMetricDataTypeBatch(systemMetricDataTypeService);
        systemMetricDataTypeBatch.update();

        Map<MetricDataName, MetricDataType> metricDataTypeMap = systemMetricDataTypeService.copyMetricDataTypeMap();
        assertEquals(metricDataTypeMap.size(), 15);
        assertEquals(metricDataTypeMap.containsKey(new MetricDataName("C_metricName1", "C_fieldName1")), true);
        assertEquals(metricDataTypeMap.containsKey(new MetricDataName("C_metricName2", "C_fieldName2")), true);
        assertEquals(metricDataTypeMap.containsKey(new MetricDataName("C_metricName3", "C_fieldName3")), true);
        assertEquals(metricDataTypeMap.containsKey(new MetricDataName("C_metricName4", "C_fieldName4")), true);
        assertEquals(metricDataTypeMap.containsKey(new MetricDataName("C_metricName5", "C_fieldName5")), true);

        systemMetricDataTypeBatch.update();
        assertEquals(metricDataTypeMap.size(), 15);
    }
}
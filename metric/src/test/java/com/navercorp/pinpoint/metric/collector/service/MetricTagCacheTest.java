package com.navercorp.pinpoint.metric.collector.service;

import com.navercorp.pinpoint.metric.collector.dao.MetricTagDao;
import com.navercorp.pinpoint.metric.common.model.MetricTagCollection;
import com.navercorp.pinpoint.metric.common.model.MetricTag;
import com.navercorp.pinpoint.metric.common.model.MetricTagKey;
import com.navercorp.pinpoint.metric.common.model.Tag;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * @author minwoo.jung
 */
@RunWith(MockitoJUnitRunner.class)
class MetricTagCacheTest {

    @Test
    void getMetricTag() {
        MetricTagDao metricTagDao = mock(MetricTagDao.class);
        MetricTagCache metricTagCache = new MetricTagCache(metricTagDao);

        MetricTagKey metricTagKey = new MetricTagKey("applicationId", "hostName", "metricName", "fieldName");
        when(metricTagDao.selectMetricTag(metricTagKey)).thenReturn(null);

        MetricTagCollection metricTagCollection = metricTagCache.getMetricTag(metricTagKey);

        assertNull(metricTagCollection);
    }

    @Test
    void getMetricTag2() {
        MetricTagDao metricTagDao = mock(MetricTagDao.class);
        MetricTagCache metricTagCache = new MetricTagCache(metricTagDao);

        MetricTagKey metricTagKey = new MetricTagKey("applicationId", "hostName", "metricName", "fieldName");
        MetricTagCollection metricTagCollection = new MetricTagCollection("applicationId", "hostName", "metricName", "fieldName", new ArrayList<MetricTag>(1));
        when(metricTagDao.selectMetricTag(metricTagKey)).thenReturn(metricTagCollection);

        MetricTagCollection returnData = metricTagCache.getMetricTag(metricTagKey);

        assertEquals(returnData, metricTagCollection);
    }

    @Test
    void saveMetricTag() {
        MetricTagDao metricTagDao = mock(MetricTagDao.class);
        MetricTagCache metricTagCache = new MetricTagCache(metricTagDao);

        MetricTag metricTag = new MetricTag("applicationId", "hostName", "metricName", "fieldName", new ArrayList<Tag>());
        metricTagCache.saveMetricTag(metricTag);

        verify(metricTagDao, times(1)).insertMetricTag(metricTag);
    }

    @Test
    void updateCacheforMetricTag() {
        MetricTagDao metricTagDao = mock(MetricTagDao.class);
        MetricTagCache metricTagCache = new MetricTagCache(metricTagDao);

        MetricTagKey metricTagKey = new MetricTagKey("applicationId", "hostName", "metricName", "fieldName");
        MetricTagCollection metricTagCollection = new MetricTagCollection("applicationId", "hostName", "metricName", "fieldName", new ArrayList<MetricTag>(1));
        MetricTagCollection metricTagCollectionResult = metricTagCache.updateCacheforMetricTag(metricTagKey, metricTagCollection);

        assertEquals(metricTagCollection, metricTagCollectionResult);
    }
}
package com.navercorp.pinpoint.metric.collector.service;

import com.navercorp.pinpoint.metric.collector.dao.MetricTagDao;
import com.navercorp.pinpoint.metric.common.model.MetricTagCollection;
import com.navercorp.pinpoint.metric.common.model.MetricTag;
import com.navercorp.pinpoint.metric.common.model.MetricTagKey;
import com.navercorp.pinpoint.metric.common.model.Tag;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.ArrayList;

import static org.mockito.Mockito.*;

/**
 * @author minwoo.jung
 */
@RunWith(MockitoJUnitRunner.class)
public class MetricTagCacheTest {

    @Test
    public void getMetricTag() {
        MetricTagDao metricTagDao = mock(MetricTagDao.class);
        MetricTagCache metricTagCache = new MetricTagCache(metricTagDao);

        MetricTagKey metricTagKey = new MetricTagKey("applicationId", "hostName", "metricName", "fieldName");
        when(metricTagDao.selectMetricTag(metricTagKey)).thenReturn(null);

        MetricTagCollection metricTagCollection = metricTagCache.getMetricTag(metricTagKey);

        Assert.assertNull(metricTagCollection);
    }

    @Test
    public void getMetricTag2() {
        MetricTagDao metricTagDao = mock(MetricTagDao.class);
        MetricTagCache metricTagCache = new MetricTagCache(metricTagDao);

        MetricTagKey metricTagKey = new MetricTagKey("applicationId", "hostName", "metricName", "fieldName");
        MetricTagCollection metricTagCollection = new MetricTagCollection("applicationId", "hostName", "metricName", "fieldName", new ArrayList<MetricTag>(1));
        when(metricTagDao.selectMetricTag(metricTagKey)).thenReturn(metricTagCollection);

        MetricTagCollection returnData = metricTagCache.getMetricTag(metricTagKey);

        Assert.assertEquals(returnData, metricTagCollection);
    }

    @Test
    public void saveMetricTag() {
        MetricTagDao metricTagDao = mock(MetricTagDao.class);
        MetricTagCache metricTagCache = new MetricTagCache(metricTagDao);

        MetricTag metricTag = new MetricTag("applicationId", "hostName", "metricName", "fieldName", new ArrayList<Tag>());
        metricTagCache.saveMetricTag(metricTag);

        verify(metricTagDao, times(1)).insertMetricTag(metricTag);
    }

    @Test
    public void updateCacheforMetricTag() {
        MetricTagDao metricTagDao = mock(MetricTagDao.class);
        MetricTagCache metricTagCache = new MetricTagCache(metricTagDao);

        MetricTagKey metricTagKey = new MetricTagKey("applicationId", "hostName", "metricName", "fieldName");
        MetricTagCollection metricTagCollection = new MetricTagCollection("applicationId", "hostName", "metricName", "fieldName", new ArrayList<MetricTag>(1));
        MetricTagCollection metricTagCollectionResult = metricTagCache.updateCacheForMetricTag(metricTagKey, metricTagCollection);

        Assert.assertEquals(metricTagCollection, metricTagCollectionResult);
    }
}
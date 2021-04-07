package com.navercorp.pinpoint.metric.collector.service;

import com.navercorp.pinpoint.metric.common.model.MetricTagCollection;
import com.navercorp.pinpoint.metric.common.model.*;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.times;


/**
 * @author minwoo.jung
 */
@RunWith(MockitoJUnitRunner.class)
public class SystemMetricTagServiceImplTest {


    @Test
    public void saveMetricTag() {
        MetricTagCache metricTagCache = mock(MetricTagCache.class);
        SystemMetricTagServiceImpl systemMetricTagService = new SystemMetricTagServiceImpl(metricTagCache);

        String applicationName = "applicationName";
        String hostName = "hostName";
        String metricName = "metricName";
        String fieldName = "fieldName";
        List<Tag> tagList = new ArrayList<>();
        tagList.add(new Tag("key", "value"));

        MetricTagKey metricTagKey = new MetricTagKey(applicationName, hostName, metricName, fieldName);
        when(metricTagCache.getMetricTag(metricTagKey)).thenReturn(null);

        SystemMetric systemMetric = new LongMetric(metricName, hostName, fieldName, 0, tagList, Long.MAX_VALUE);
        systemMetricTagService.saveMetricTag(applicationName, systemMetric);


        MetricTag metricTag = new MetricTag(applicationName, hostName, metricName, fieldName, tagList);
        List<MetricTag> metricTagList = new ArrayList<>(1);
        metricTagList.add(metricTag);
        MetricTagCollection metricTagCollection = new MetricTagCollection(applicationName, hostName, metricName, fieldName, metricTagList);

        verify(metricTagCache, times(1)).updateCacheForMetricTag(metricTagKey, metricTagCollection);
        verify(metricTagCache, times(1)).saveMetricTag(metricTag);
    }

    @Test
    public void saveMetricTag2() {
        MetricTagCache metricTagCache = mock(MetricTagCache.class);
        SystemMetricTagServiceImpl systemMetricTagService = new SystemMetricTagServiceImpl(metricTagCache);

        String applicationName = "applicationName";
        String hostName = "hostName";
        String metricName = "metricName";
        String fieldName = "fieldName";
        List<Tag> tagList = new ArrayList<>();
        tagList.add(new Tag("key", "value"));

        MetricTagKey metricTagKey = new MetricTagKey(applicationName, hostName, metricName, fieldName);
        MetricTag metricTag = new MetricTag(applicationName, hostName, metricName, fieldName, tagList);
        List<MetricTag> metricTagList = new ArrayList<>(1);
        metricTagList.add(metricTag);
        MetricTagCollection metricTagCollection = new MetricTagCollection(applicationName, hostName, metricName, fieldName, metricTagList);
        when(metricTagCache.getMetricTag(metricTagKey)).thenReturn(metricTagCollection);

        SystemMetric systemMetric = new LongMetric(metricName, hostName, fieldName, 0, tagList, Long.MAX_VALUE);
        systemMetricTagService.saveMetricTag(applicationName, systemMetric);

        verify(metricTagCache, times(0)).updateCacheForMetricTag(any(MetricTagKey.class), any(MetricTagCollection.class));
        verify(metricTagCache, times(0)).saveMetricTag(any(MetricTag.class));
    }


    @Test
    public void saveMetricTag3() {
        MetricTagCache metricTagCache = mock(MetricTagCache.class);
        SystemMetricTagServiceImpl systemMetricTagService = new SystemMetricTagServiceImpl(metricTagCache);

        String applicationName = "applicationName";
        String hostName = "hostName";
        String metricName = "metricName";
        String fieldName = "fieldName";
        List<Tag> tagList = new ArrayList<>();
        tagList.add(new Tag("key", "value"));

        MetricTagKey metricTagKey = new MetricTagKey(applicationName, hostName, metricName, fieldName);
        MetricTag metricTag = new MetricTag(applicationName, hostName, metricName, fieldName, tagList);
        List<MetricTag> metricTagList = new ArrayList<>(1);
        metricTagList.add(metricTag);
        MetricTagCollection metricTagCollection = new MetricTagCollection(applicationName, hostName, metricName, fieldName, metricTagList);
        when(metricTagCache.getMetricTag(metricTagKey)).thenReturn(metricTagCollection);

        SystemMetric systemMetric = new LongMetric(metricName, hostName, fieldName, 0, new ArrayList<>(), Long.MAX_VALUE);
        systemMetricTagService.saveMetricTag(applicationName, systemMetric);

        verify(metricTagCache, times(1)).updateCacheForMetricTag(any(MetricTagKey.class), any(MetricTagCollection.class));
        verify(metricTagCache, times(1)).saveMetricTag(any(MetricTag.class));
    }

    @Test
    public void createMetricTagCollection() {
        MetricTagCache metricTagCache = mock(MetricTagCache.class);
        SystemMetricTagServiceImpl systemMetricTagService = new SystemMetricTagServiceImpl(metricTagCache);

        String applicationName = "applicationName";
        String hostName = "hostName";
        String metricName = "metricName";
        String fieldName = "fieldName";
        List<Tag> tagList = new ArrayList<>();
        tagList.add(new Tag("key", "value"));
        tagList.add(new Tag("key2", "value2"));

        MetricTagCollection metricTagCollection = systemMetricTagService.createMetricTagCollection(applicationName, hostName, metricName, fieldName, tagList);
        Assert.assertEquals(metricTagCollection.getHostGroupName(), applicationName);
        Assert.assertEquals(metricTagCollection.getHostName(), hostName);
        Assert.assertEquals(metricTagCollection.getMetricName(), metricName);
        Assert.assertEquals(metricTagCollection.getFieldName(), fieldName);

        List<MetricTag> metricTagList = metricTagCollection.getMetricTagList();
        Assert.assertEquals(metricTagList.size(), 1);
        MetricTag metricTag = metricTagList.get(0);
        Assert.assertEquals(metricTag.getHostGroupName(), applicationName);
        Assert.assertEquals(metricTag.getHostName(), hostName);
        Assert.assertEquals(metricTag.getMetricName(), metricName);
        Assert.assertEquals(metricTag.getFieldName(), fieldName);

        List<Tag> tags = metricTag.getTags();
        Assert.assertEquals(tags.size(), 2);
        Assert.assertEquals(tags, tagList);
    }

    @Test
    public void createMetricTagCollection2() {
        MetricTagCache metricTagCache = mock(MetricTagCache.class);
        SystemMetricTagServiceImpl systemMetricTagService = new SystemMetricTagServiceImpl(metricTagCache);

        String applicationName = "applicationName";
        String hostName = "hostName";
        String metricName = "metricName";
        String fieldName = "fieldName";

        MetricTag metricTag = new MetricTag(applicationName, hostName, metricName, fieldName, new ArrayList<>());
        List<Tag> tagList2 = new ArrayList<>();
        tagList2.add(new Tag("key", "value"));
        tagList2.add(new Tag("key2", "value2"));
        MetricTag metricTag2 = new MetricTag(applicationName, hostName, metricName, fieldName, tagList2);

        List<Tag> tagList3 = new ArrayList<>();
        tagList3.add(new Tag("key", "value"));
        tagList3.add(new Tag("key2", "value2"));
        tagList3.add(new Tag("key3", "value3"));
        MetricTag metricTag3 = new MetricTag(applicationName, hostName, metricName, fieldName, tagList3);

        List<MetricTag> metricTagList = new ArrayList<>();
        metricTagList.add(metricTag);
        metricTagList.add(metricTag2);
        metricTagList.add(metricTag3);

        List<Tag> tagList4 = new ArrayList<>();
        tagList4.add(new Tag("key", "value"));
        tagList4.add(new Tag("key2", "value2"));
        tagList4.add(new Tag("key3", "value3"));
        tagList4.add(new Tag("key4", "value4"));
        MetricTagCollection mtc = new MetricTagCollection(applicationName, hostName, metricName, fieldName, metricTagList);
        MetricTagCollection metricTagCollection = systemMetricTagService.createMetricTagCollection(mtc, tagList4);

        Assert.assertEquals(metricTagCollection.getHostGroupName(), applicationName);
        Assert.assertEquals(metricTagCollection.getHostName(), hostName);
        Assert.assertEquals(metricTagCollection.getMetricName(), metricName);
        Assert.assertEquals(metricTagCollection.getFieldName(), fieldName);

        List<MetricTag> mtList = metricTagCollection.getMetricTagList();
        Assert.assertEquals(mtList.size(), 4);
    }

    @Test
    public void tagListCopyAndEquals() {
        List<Tag> tagList = new ArrayList<>();
        tagList.add(new Tag("key", "value"));
        tagList.add(new Tag("key2", "value2"));
        tagList.add(new Tag("key3", "value3"));

        MetricTagCache metricTagCache = mock(MetricTagCache.class);
        SystemMetricTagServiceImpl systemMetricTagService = new SystemMetricTagServiceImpl(metricTagCache);

        List<Tag> tags = systemMetricTagService.tagListCopy(tagList);

        Assert.assertEquals(tags, tagList);
    }


}
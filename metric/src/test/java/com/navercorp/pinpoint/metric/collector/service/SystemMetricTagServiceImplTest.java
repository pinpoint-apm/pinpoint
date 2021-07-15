package com.navercorp.pinpoint.metric.collector.service;

import com.navercorp.pinpoint.metric.common.model.MetricTagCollection;
import com.navercorp.pinpoint.metric.common.model.*;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
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
        List<Tag> tagList = new ArrayList<Tag>();
        tagList.add(new Tag("key", "value"));

        MetricTagKey metricTagKey = new MetricTagKey(applicationName, hostName, metricName, fieldName);
        when(metricTagCache.getMetricTag(metricTagKey)).thenReturn(null);

        SystemMetric systemMetric = new LongCounter(metricName, hostName, fieldName, 0, tagList, Long.MAX_VALUE);
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
        List<Tag> tagList = new ArrayList<Tag>();
        tagList.add(new Tag("key", "value"));

        MetricTagKey metricTagKey = new MetricTagKey(applicationName, hostName, metricName, fieldName);
        MetricTag metricTag = new MetricTag(applicationName, hostName, metricName, fieldName, tagList);
        List<MetricTag> metricTagList = new ArrayList<>(1);
        metricTagList.add(metricTag);
        MetricTagCollection metricTagCollection = new MetricTagCollection(applicationName, hostName, metricName, fieldName, metricTagList);
        when(metricTagCache.getMetricTag(metricTagKey)).thenReturn(metricTagCollection);

        SystemMetric systemMetric = new LongCounter(metricName, hostName, fieldName, 0, tagList, Long.MAX_VALUE);
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
        List<Tag> tagList = new ArrayList<Tag>();
        tagList.add(new Tag("key", "value"));

        MetricTagKey metricTagKey = new MetricTagKey(applicationName, hostName, metricName, fieldName);
        MetricTag metricTag = new MetricTag(applicationName, hostName, metricName, fieldName, tagList);
        List<MetricTag> metricTagList = new ArrayList<>(1);
        metricTagList.add(metricTag);
        MetricTagCollection metricTagCollection = new MetricTagCollection(applicationName, hostName, metricName, fieldName, metricTagList);
        when(metricTagCache.getMetricTag(metricTagKey)).thenReturn(metricTagCollection);

        SystemMetric systemMetric = new LongCounter(metricName, hostName, fieldName, 0, new ArrayList<Tag>(), Long.MAX_VALUE);
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
        List<Tag> tagList = new ArrayList<Tag>();
        tagList.add(new Tag("key", "value"));
        tagList.add(new Tag("key2", "value2"));

        MetricTagCollection metricTagCollection = systemMetricTagService.createMetricTagCollection(applicationName, hostName, metricName, fieldName, tagList);
        assertEquals(metricTagCollection.getHostGroupId(), applicationName);
        assertEquals(metricTagCollection.getHostName(), hostName);
        assertEquals(metricTagCollection.getMetricName(), metricName);
        assertEquals(metricTagCollection.getFieldName(), fieldName);

        List<MetricTag> metricTagList = metricTagCollection.getMetricTagList();
        assertEquals(metricTagList.size(), 1);
        MetricTag metricTag = metricTagList.get(0);
        assertEquals(metricTag.getHostGroupId(), applicationName);
        assertEquals(metricTag.getHostName(), hostName);
        assertEquals(metricTag.getMetricName(), metricName);
        assertEquals(metricTag.getFieldName(), fieldName);

        List<Tag> tags = metricTag.getTags();
        assertEquals(tags.size(), 2);
        assertEquals(tags, tagList);
    }

    @Test
    public void createMetricTagCollection2() {
        MetricTagCache metricTagCache = mock(MetricTagCache.class);
        SystemMetricTagServiceImpl systemMetricTagService = new SystemMetricTagServiceImpl(metricTagCache);

        String applicationName = "applicationName";
        String hostName = "hostName";
        String metricName = "metricName";
        String fieldName = "fieldName";

        MetricTag metricTag = new MetricTag(applicationName, hostName, metricName, fieldName, new ArrayList<Tag>());
        List<Tag> tagList2 = new ArrayList<Tag>();
        tagList2.add(new Tag("key", "value"));
        tagList2.add(new Tag("key2", "value2"));
        MetricTag metricTag2 = new MetricTag(applicationName, hostName, metricName, fieldName, tagList2);

        List<Tag> tagList3 = new ArrayList<Tag>();
        tagList3.add(new Tag("key", "value"));
        tagList3.add(new Tag("key2", "value2"));
        tagList3.add(new Tag("key3", "value3"));
        MetricTag metricTag3 = new MetricTag(applicationName, hostName, metricName, fieldName, tagList3);

        List<MetricTag> metricTagList = new ArrayList<MetricTag>();
        metricTagList.add(metricTag);
        metricTagList.add(metricTag2);
        metricTagList.add(metricTag3);

        List<Tag> tagList4 = new ArrayList<Tag>();
        tagList4.add(new Tag("key", "value"));
        tagList4.add(new Tag("key2", "value2"));
        tagList4.add(new Tag("key3", "value3"));
        tagList4.add(new Tag("key4", "value4"));
        MetricTagCollection mtc = new MetricTagCollection(applicationName, hostName, metricName, fieldName, metricTagList);
        MetricTagCollection metricTagCollection = systemMetricTagService.createMetricTagCollection(mtc, tagList4);

        assertEquals(metricTagCollection.getHostGroupId(), applicationName);
        assertEquals(metricTagCollection.getHostName(), hostName);
        assertEquals(metricTagCollection.getMetricName(), metricName);
        assertEquals(metricTagCollection.getFieldName(), fieldName);

        List<MetricTag> mtList = metricTagCollection.getMetricTagList();
        assertEquals(mtList.size(), 4);
    }

    @Test
    public void tagListCopyAndEquals() {
        List<Tag> tagList = new ArrayList<Tag>();
        tagList.add(new Tag("key", "value"));
        tagList.add(new Tag("key2", "value2"));
        tagList.add(new Tag("key3", "value3"));

        MetricTagCache metricTagCache = mock(MetricTagCache.class);
        SystemMetricTagServiceImpl systemMetricTagService = new SystemMetricTagServiceImpl(metricTagCache);

        List<Tag> tags = systemMetricTagService.tagListCopy(tagList);

        assertEquals(tags, tagList);
    }


}
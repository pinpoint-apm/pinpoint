package com.navercorp.pinpoint.metric.collector.service;

import com.navercorp.pinpoint.metric.common.model.DoubleMetric;
import com.navercorp.pinpoint.metric.common.model.MetricTag;
import com.navercorp.pinpoint.metric.common.model.MetricTagCollection;
import com.navercorp.pinpoint.metric.common.model.MetricTagKey;
import com.navercorp.pinpoint.metric.common.model.SystemMetric;
import com.navercorp.pinpoint.metric.common.model.Tag;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
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
public class SystemMetricTagServiceImplTest {


    @Test
    public void saveMetricTag() {
        MetricTagCache metricTagCache = mock(MetricTagCache.class);
        SystemMetricTagServiceImpl systemMetricTagService = new SystemMetricTagServiceImpl(metricTagCache);

        String applicationName = "applicationName";
        String hostName = "hostName";
        String metricName = "metricName";
        String fieldName = "fieldName";
        long saveTime = getSaveTime();
        List<Tag> tagList = new ArrayList<>();
        tagList.add(new Tag("key", "value"));

        MetricTagKey metricTagKey = new MetricTagKey(applicationName, hostName, metricName, fieldName, saveTime);
        when(metricTagCache.getMetricTag(metricTagKey)).thenReturn(null);

        SystemMetric systemMetric = new DoubleMetric(metricName, hostName, fieldName, 0, tagList, Long.MAX_VALUE);
        systemMetricTagService.saveMetricTag(applicationName, systemMetric);


        MetricTag metricTag = new MetricTag(applicationName, hostName, metricName, fieldName, tagList, saveTime);
        List<MetricTag> metricTagList = new ArrayList<>(1);
        metricTagList.add(metricTag);
        MetricTagCollection metricTagCollection = new MetricTagCollection(applicationName, hostName, metricName, fieldName, metricTagList);

        verify(metricTagCache).updateCacheForMetricTag(metricTagKey, metricTagCollection);
        verify(metricTagCache).saveMetricTag(metricTag);
    }

    @Test
    public void saveMetricTag2() {
        MetricTagCache metricTagCache = mock(MetricTagCache.class);
        SystemMetricTagServiceImpl systemMetricTagService = new SystemMetricTagServiceImpl(metricTagCache);

        String applicationName = "applicationName";
        String hostName = "hostName";
        String metricName = "metricName";
        String fieldName = "fieldName";
        long saveTime = getSaveTime();
        List<Tag> tagList = new ArrayList<>();
        tagList.add(new Tag("key", "value"));

        MetricTagKey metricTagKey = new MetricTagKey(applicationName, hostName, metricName, fieldName, saveTime);
        MetricTag metricTag = new MetricTag(applicationName, hostName, metricName, fieldName, tagList, saveTime);
        List<MetricTag> metricTagList = new ArrayList<>(1);
        metricTagList.add(metricTag);
        MetricTagCollection metricTagCollection = new MetricTagCollection(applicationName, hostName, metricName, fieldName, metricTagList);
        when(metricTagCache.getMetricTag(metricTagKey)).thenReturn(metricTagCollection);

        SystemMetric systemMetric = new DoubleMetric(metricName, hostName, fieldName, 0, tagList, Long.MAX_VALUE);
        systemMetricTagService.saveMetricTag(applicationName, systemMetric);

        verify(metricTagCache, never()).updateCacheForMetricTag(any(MetricTagKey.class), any(MetricTagCollection.class));
        verify(metricTagCache, never()).saveMetricTag(any(MetricTag.class));
    }


    @Test
    public void saveMetricTag3() {
        MetricTagCache metricTagCache = mock(MetricTagCache.class);
        SystemMetricTagServiceImpl systemMetricTagService = new SystemMetricTagServiceImpl(metricTagCache);

        String applicationName = "applicationName";
        String hostName = "hostName";
        String metricName = "metricName";
        String fieldName = "fieldName";
        long saveTime = getSaveTime();
        List<Tag> tagList = new ArrayList<>();
        tagList.add(new Tag("key", "value"));

        MetricTagKey metricTagKey = new MetricTagKey(applicationName, hostName, metricName, fieldName, saveTime);
        MetricTag metricTag = new MetricTag(applicationName, hostName, metricName, fieldName, tagList, saveTime);
        List<MetricTag> metricTagList = new ArrayList<>(1);
        metricTagList.add(metricTag);
        MetricTagCollection metricTagCollection = new MetricTagCollection(applicationName, hostName, metricName, fieldName, metricTagList);
        when(metricTagCache.getMetricTag(metricTagKey)).thenReturn(metricTagCollection);

        SystemMetric systemMetric = new DoubleMetric(metricName, hostName, fieldName, 0, new ArrayList<>(), Long.MAX_VALUE);
        systemMetricTagService.saveMetricTag(applicationName, systemMetric);

        verify(metricTagCache).updateCacheForMetricTag(any(MetricTagKey.class), any(MetricTagCollection.class));
        verify(metricTagCache).saveMetricTag(any(MetricTag.class));
    }

    @Test
    public void createMetricTagCollection() {
        MetricTagCache metricTagCache = mock(MetricTagCache.class);
        SystemMetricTagServiceImpl systemMetricTagService = new SystemMetricTagServiceImpl(metricTagCache);

        String applicationName = "applicationName";
        String hostName = "hostName";
        String metricName = "metricName";
        String fieldName = "fieldName";
        long saveTime = new Date().getTime();
        List<Tag> tagList = new ArrayList<>();
        tagList.add(new Tag("key", "value"));
        tagList.add(new Tag("key2", "value2"));

        MetricTagCollection metricTagCollection = systemMetricTagService.createMetricTagCollection(applicationName, hostName, metricName, fieldName, tagList, saveTime);
        Assertions.assertEquals(metricTagCollection.getHostGroupName(), applicationName);
        Assertions.assertEquals(metricTagCollection.getHostName(), hostName);
        Assertions.assertEquals(metricTagCollection.getMetricName(), metricName);
        Assertions.assertEquals(metricTagCollection.getFieldName(), fieldName);

        List<MetricTag> metricTagList = metricTagCollection.getMetricTagList();
        Assertions.assertEquals(metricTagList.size(), 1);
        MetricTag metricTag = metricTagList.get(0);
        Assertions.assertEquals(metricTag.getHostGroupName(), applicationName);
        Assertions.assertEquals(metricTag.getHostName(), hostName);
        Assertions.assertEquals(metricTag.getMetricName(), metricName);
        Assertions.assertEquals(metricTag.getFieldName(), fieldName);

        List<Tag> tags = metricTag.getTags();
        Assertions.assertEquals(tags.size(), 2);
        Assertions.assertEquals(tags, tagList);
    }

    @Test
    public void createMetricTagCollection2() {
        MetricTagCache metricTagCache = mock(MetricTagCache.class);
        SystemMetricTagServiceImpl systemMetricTagService = new SystemMetricTagServiceImpl(metricTagCache);

        String applicationName = "applicationName";
        String hostName = "hostName";
        String metricName = "metricName";
        String fieldName = "fieldName";
        long saveTime = new Date().getTime();

        MetricTag metricTag = new MetricTag(applicationName, hostName, metricName, fieldName, new ArrayList<>(), saveTime);
        List<Tag> tagList2 = new ArrayList<>();
        tagList2.add(new Tag("key", "value"));
        tagList2.add(new Tag("key2", "value2"));
        MetricTag metricTag2 = new MetricTag(applicationName, hostName, metricName, fieldName, tagList2, saveTime);

        List<Tag> tagList3 = new ArrayList<>();
        tagList3.add(new Tag("key", "value"));
        tagList3.add(new Tag("key2", "value2"));
        tagList3.add(new Tag("key3", "value3"));
        MetricTag metricTag3 = new MetricTag(applicationName, hostName, metricName, fieldName, tagList3, saveTime);

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
        MetricTagCollection metricTagCollection = systemMetricTagService.createMetricTagCollection(mtc, tagList4, saveTime);

        Assertions.assertEquals(metricTagCollection.getHostGroupName(), applicationName);
        Assertions.assertEquals(metricTagCollection.getHostName(), hostName);
        Assertions.assertEquals(metricTagCollection.getMetricName(), metricName);
        Assertions.assertEquals(metricTagCollection.getFieldName(), fieldName);

        List<MetricTag> mtList = metricTagCollection.getMetricTagList();
        Assertions.assertEquals(mtList.size(), 4);
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

        Assertions.assertEquals(tags, tagList);
    }

    private long getSaveTime() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.clear(Calendar.MINUTE);
        calendar.clear(Calendar.SECOND);
        calendar.clear(Calendar.MILLISECOND);
        calendar.set(Calendar.DAY_OF_WEEK, calendar.getFirstDayOfWeek());

        return calendar.getTimeInMillis();
    }

}
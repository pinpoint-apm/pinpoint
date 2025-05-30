package com.navercorp.pinpoint.metric.collector.service;

import com.navercorp.pinpoint.common.timeseries.time.DateTimeUtils;
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

import java.util.Date;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
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

        String tenantId = "tenantId";
        String applicationName = "applicationName";
        String hostName = "hostName";
        String metricName = "metricName";
        String fieldName = "fieldName";
        long saveTime = getSaveTime();
        List<Tag> tagList = List.of(new Tag("key", "value"));

        MetricTagKey metricTagKey = new MetricTagKey(tenantId, applicationName, hostName, metricName, fieldName, saveTime);
        when(metricTagCache.getMetricTag(metricTagKey)).thenReturn(null);

        SystemMetric systemMetric = new DoubleMetric(metricName, hostName, fieldName, 0, tagList, Long.MAX_VALUE);
        systemMetricTagService.saveMetricTag(tenantId, applicationName, systemMetric);


        MetricTag metricTag = new MetricTag(tenantId, applicationName, hostName, metricName, fieldName, tagList, saveTime);

        List<MetricTag> metricTagList = List.of(metricTag);
        MetricTagCollection metricTagCollection = new MetricTagCollection(tenantId, applicationName, hostName, metricName, fieldName, metricTagList);

        verify(metricTagCache).updateCacheForMetricTag(metricTagKey, metricTagCollection);
        verify(metricTagCache).saveMetricTag(metricTag);
    }

    @Test
    public void saveMetricTag2() {
        MetricTagCache metricTagCache = mock(MetricTagCache.class);
        SystemMetricTagServiceImpl systemMetricTagService = new SystemMetricTagServiceImpl(metricTagCache);

        String tenantId = "tenantId";
        String applicationName = "applicationName";
        String hostName = "hostName";
        String metricName = "metricName";
        String fieldName = "fieldName";
        long saveTime = getSaveTime();
        List<Tag> tagList = List.of(new Tag("key", "value"));

        MetricTagKey metricTagKey = new MetricTagKey(tenantId, applicationName, hostName, metricName, fieldName, saveTime);
        MetricTag metricTag = new MetricTag(tenantId, applicationName, hostName, metricName, fieldName, tagList, saveTime);

        List<MetricTag> metricTagList = List.of(metricTag);
        MetricTagCollection metricTagCollection = new MetricTagCollection(tenantId, applicationName, hostName, metricName, fieldName, metricTagList);
        when(metricTagCache.getMetricTag(metricTagKey)).thenReturn(metricTagCollection);

        SystemMetric systemMetric = new DoubleMetric(metricName, hostName, fieldName, 0, tagList, Long.MAX_VALUE);
        systemMetricTagService.saveMetricTag(tenantId, applicationName, systemMetric);

        verify(metricTagCache, never()).updateCacheForMetricTag(any(MetricTagKey.class), any(MetricTagCollection.class));
        verify(metricTagCache, never()).saveMetricTag(any(MetricTag.class));
    }


    @Test
    public void saveMetricTag3() {
        MetricTagCache metricTagCache = mock(MetricTagCache.class);
        SystemMetricTagServiceImpl systemMetricTagService = new SystemMetricTagServiceImpl(metricTagCache);

        String tenantId = "tenantId";
        String applicationName = "applicationName";
        String hostName = "hostName";
        String metricName = "metricName";
        String fieldName = "fieldName";
        long saveTime = getSaveTime();
        List<Tag> tagList = List.of(new Tag("key", "value"));

        MetricTagKey metricTagKey = new MetricTagKey(tenantId, applicationName, hostName, metricName, fieldName, saveTime);
        MetricTag metricTag = new MetricTag(tenantId, applicationName, hostName, metricName, fieldName, tagList, saveTime);

        List<MetricTag> metricTagList = List.of(metricTag);
        MetricTagCollection metricTagCollection = new MetricTagCollection(tenantId, applicationName, hostName, metricName, fieldName, metricTagList);
        when(metricTagCache.getMetricTag(metricTagKey)).thenReturn(metricTagCollection);

        SystemMetric systemMetric = new DoubleMetric(metricName, hostName, fieldName, 0, List.of(), Long.MAX_VALUE);
        systemMetricTagService.saveMetricTag(tenantId, applicationName, systemMetric);

        verify(metricTagCache).updateCacheForMetricTag(any(MetricTagKey.class), any(MetricTagCollection.class));
        verify(metricTagCache).saveMetricTag(any(MetricTag.class));
    }

    @Test
    public void createMetricTagCollection() {
        MetricTagCache metricTagCache = mock(MetricTagCache.class);
        SystemMetricTagServiceImpl systemMetricTagService = new SystemMetricTagServiceImpl(metricTagCache);

        String tenantId = "tenantId";
        String applicationName = "applicationName";
        String hostName = "hostName";
        String metricName = "metricName";
        String fieldName = "fieldName";
        long saveTime = System.currentTimeMillis();
        List<Tag> tagList = List.of(
                new Tag("key", "value"),
                new Tag("key2", "value2")
        );

        MetricTagCollection metricTagCollection = systemMetricTagService.createMetricTagCollection(tenantId, applicationName, hostName, metricName, fieldName, tagList, saveTime);
        Assertions.assertEquals(applicationName, metricTagCollection.getHostGroupName());
        Assertions.assertEquals(hostName, metricTagCollection.getHostName());
        Assertions.assertEquals(metricName, metricTagCollection.getMetricName());
        Assertions.assertEquals(fieldName, metricTagCollection.getFieldName());

        List<MetricTag> metricTagList = metricTagCollection.getMetricTagList();

        assertThat(metricTagList).hasSize(1);
        MetricTag metricTag = metricTagList.get(0);
        Assertions.assertEquals(applicationName, metricTag.getHostGroupName());
        Assertions.assertEquals(hostName, metricTag.getHostName());
        Assertions.assertEquals(metricName, metricTag.getMetricName());
        Assertions.assertEquals(fieldName, metricTag.getFieldName());

        List<Tag> tags = metricTag.getTags();

        assertThat(tags)
                .containsExactlyElementsOf(tagList);
    }

    @Test
    public void createMetricTagCollection2() {
        MetricTagCache metricTagCache = mock(MetricTagCache.class);
        SystemMetricTagServiceImpl systemMetricTagService = new SystemMetricTagServiceImpl(metricTagCache);

        String tenantId = "tenantId";
        String applicationName = "applicationName";
        String hostName = "hostName";
        String metricName = "metricName";
        String fieldName = "fieldName";
        long saveTime = new Date().getTime();

        MetricTag metricTag = new MetricTag(tenantId, applicationName, hostName, metricName, fieldName, List.of(), saveTime);
        List<Tag> tagList2 = List.of(
                new Tag("key", "value"),
                new Tag("key2", "value2")
        );
        MetricTag metricTag2 = new MetricTag(tenantId, applicationName, hostName, metricName, fieldName, tagList2, saveTime);

        List<Tag> tagList3 = List.of(
                new Tag("key", "value"),
                new Tag("key2", "value2"),
                new Tag("key3", "value3")
        );
        MetricTag metricTag3 = new MetricTag(tenantId, applicationName, hostName, metricName, fieldName, tagList3, saveTime);

        List<MetricTag> metricTagList = List.of(metricTag, metricTag2, metricTag3);

        List<Tag> tagList4 = List.of(
                new Tag("key", "value"),
                new Tag("key2", "value2"),
                new Tag("key3", "value3"),
                new Tag("key4", "value4")
        );
        MetricTagCollection mtc = new MetricTagCollection(tenantId, applicationName, hostName, metricName, fieldName, metricTagList);
        MetricTagCollection metricTagCollection = systemMetricTagService.createMetricTagCollection(mtc, tagList4, saveTime);

        Assertions.assertEquals(applicationName, metricTagCollection.getHostGroupName());
        Assertions.assertEquals(hostName, metricTagCollection.getHostName());
        Assertions.assertEquals(metricName, metricTagCollection.getMetricName());
        Assertions.assertEquals(fieldName, metricTagCollection.getFieldName());

        List<MetricTag> mtList = metricTagCollection.getMetricTagList();
        assertThat(mtList).hasSize(4);
    }

    @Test
    public void tagListCopyAndEquals() {
        List<Tag> tagList = List.of(
                new Tag("key", "value"),
                new Tag("key2", "value2"),
                new Tag("key3", "value3")
        );

        MetricTagCache metricTagCache = mock(MetricTagCache.class);
        SystemMetricTagServiceImpl systemMetricTagService = new SystemMetricTagServiceImpl(metricTagCache);

        List<Tag> tags = systemMetricTagService.tagListCopy(tagList);

        Assertions.assertEquals(tags, tagList);
    }

    private long getSaveTime() {
        return DateTimeUtils.previousOrSameSundayToMillis();
    }

}
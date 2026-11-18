package com.navercorp.pinpoint.metric.web.service;

import com.navercorp.pinpoint.common.timeseries.time.Range;
import com.navercorp.pinpoint.common.timeseries.window.TimeWindow;
import com.navercorp.pinpoint.common.timeseries.window.TimeWindowSlotCentricSampler;
import com.navercorp.pinpoint.metric.common.model.MetricTag;
import com.navercorp.pinpoint.metric.common.model.MetricTagCollection;
import com.navercorp.pinpoint.metric.common.model.MetricTagKey;
import com.navercorp.pinpoint.metric.common.model.Tag;
import com.navercorp.pinpoint.metric.web.dao.SystemMetricHostInfoDao;
import com.navercorp.pinpoint.metric.web.mapping.Field;
import com.navercorp.pinpoint.metric.web.model.MetricDataSearchKey;
import com.navercorp.pinpoint.metric.web.model.basic.metric.group.MatchingRule;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @author minwoo.jung
 */
public class SystemMetricHostInfoServiceImplTest {
    private final Logger logger = LogManager.getLogger(this.getClass());

    @Test
    public void test() {
        List<Tag> tagList = List.of(
                new Tag("key", "value")
        );

        List<Tag> comparedTagList = new ArrayList<>();

        assertThat(tagList).containsAll(comparedTagList);
    }

    @Test
    public void getTag_exactOne_legacyAndDefaultRows() {
        List<Tag> tags = List.of(new Tag("cpu", "cpu-total"));
        MetricTag legacy = new MetricTag("tenantId", "", "hostGroup", "host", "cpu", "usage_user", tags, 0);
        MetricTag current = new MetricTag("tenantId", "DEFAULT", "hostGroup", "host", "cpu", "usage_user", tags, 0);
        SystemMetricHostInfoDao dao = mock(SystemMetricHostInfoDao.class);
        when(dao.selectMetricTagCollection(any(MetricTagKey.class)))
                .thenReturn(new MetricTagCollection("tenantId", "DEFAULT", "hostGroup", "host", "cpu", "usage_user", List.of(legacy, current)));
        SystemMetricHostInfoServiceImpl service = new SystemMetricHostInfoServiceImpl(dao, mock(YMLSystemMetricBasicGroupManager.class));

        TimeWindow timeWindow = new TimeWindow(Range.between(0, 600000), new TimeWindowSlotCentricSampler(10000L, 200));
        MetricDataSearchKey searchKey = new MetricDataSearchKey("tenantId", "DEFAULT", "hostGroup", "host", "cpu", "cpu", timeWindow);
        List<MetricTag> result = service.getTag(searchKey, new Field("usage_user", tags, MatchingRule.EXACT_ONE), List.of());

        assertThat(result).hasSize(1);
    }
}

package com.navercorp.pinpoint.web.authorization.controller;

import com.navercorp.pinpoint.web.service.appmetric.ApplicationStatChartService;
import com.navercorp.pinpoint.web.service.appmetric.MetricName;
import com.navercorp.pinpoint.web.util.TimeWindow;
import com.navercorp.pinpoint.web.vo.stat.chart.StatChart;
import org.junit.Assert;
import org.junit.Test;

import java.util.List;
import java.util.Map;

public class PathMappingBuilderTest {

    private static final String PREFIX = "TestMetric";

    @Test
    public void build() {
        PathMappingBuilder<ApplicationStatChartService> builder = new PathMappingBuilder<>(PREFIX);
        Map<String, ApplicationStatChartService> map = builder.build(List.of(new TestApplicationStatChartService()));
        Assert.assertEquals(1, map.size());
    }

    @Test
    public void parseChartType() {
        String chartType = "testChart";
        PathMappingBuilder<ApplicationStatChartService> builder = new PathMappingBuilder<>(PREFIX);
        String parseChartType = builder.parseChartType(PREFIX + chartType);

        Assert.assertEquals(chartType, parseChartType);
    }

    @MetricName(PREFIX + "testChart")
    public static class TestApplicationStatChartService implements ApplicationStatChartService {

        @Override
        public StatChart selectApplicationChart(String applicationId, TimeWindow timeWindow) {
            return null;
        }
    }
}
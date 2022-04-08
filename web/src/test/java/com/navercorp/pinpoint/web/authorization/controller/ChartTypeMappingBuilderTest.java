package com.navercorp.pinpoint.web.authorization.controller;

import com.navercorp.pinpoint.web.service.stat.ChartTypeSupport;
import org.junit.Assert;
import org.junit.Test;

import java.util.List;
import java.util.Map;

public class ChartTypeMappingBuilderTest {
    @Test
    public void build() {
        ChartTypeMappingBuilder<TestApplicationStatChartService> builder = new ChartTypeMappingBuilder<>();
        Map<String, TestApplicationStatChartService> map = builder.build(List.of(new TestApplicationStatChartService()));
        Assert.assertEquals(1, map.size());
    }

    public static class TestApplicationStatChartService implements ChartTypeSupport {

        @Override
        public String getChartType() {
            return "test";
        }
    }
}
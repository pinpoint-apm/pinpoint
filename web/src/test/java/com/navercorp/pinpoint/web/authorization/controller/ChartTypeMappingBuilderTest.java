package com.navercorp.pinpoint.web.authorization.controller;

import com.navercorp.pinpoint.web.service.stat.ChartTypeSupport;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

public class ChartTypeMappingBuilderTest {
    @Test
    public void build() {
        ChartTypeMappingBuilder<TestApplicationStatChartService> builder = new ChartTypeMappingBuilder<>();
        Map<String, TestApplicationStatChartService> map = builder.build(List.of(new TestApplicationStatChartService()));
        assertThat(map).hasSize(1);
    }

    public static class TestApplicationStatChartService implements ChartTypeSupport {

        @Override
        public String getChartType() {
            return "test";
        }
    }
}
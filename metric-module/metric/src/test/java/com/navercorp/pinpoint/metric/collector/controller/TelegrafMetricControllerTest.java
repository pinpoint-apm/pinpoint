package com.navercorp.pinpoint.metric.collector.controller;

import com.navercorp.pinpoint.metric.common.model.Tag;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class TelegrafMetricControllerTest {

    @Test
    void filterTag() {
        List<Tag> tags = List.of(
                new Tag("tag1", "value1"),
                new Tag("host", "host1")
        );
        List<Tag> tags1 = TelegrafMetricController.filterTag(tags, new String[]{"host"});
        assertThat(tags1)
                .hasSize(1)
                .containsExactly(new Tag("tag1", "value1"));
    }

    @Test
    void sanitizeForLog_null() {
        assertThat(TelegrafMetricController.sanitizeForLog(null)).isNull();
    }

    @Test
    void sanitizeForLog_replaceControlChars() {
        assertThat(TelegrafMetricController.sanitizeForLog("host\ninjected"))
                .isEqualTo("host_injected");
        assertThat(TelegrafMetricController.sanitizeForLog("host\r\ninjected"))
                .isEqualTo("host__injected");
        assertThat(TelegrafMetricController.sanitizeForLog("normal-host_name.01"))
                .isEqualTo("normal-host_name.01");
    }

    @Test
    void sanitizeForLog_truncate() {
        String longValue = "a".repeat(150);
        String result = TelegrafMetricController.sanitizeForLog(longValue);
        assertThat(result).hasSize(100 + "...(truncated)".length());
        assertThat(result).endsWith("...(truncated)");
    }

    @Test
    void sanitizeForLog_exactlyMaxLength() {
        String value = "a".repeat(100);
        assertThat(TelegrafMetricController.sanitizeForLog(value)).isEqualTo(value);
    }
}
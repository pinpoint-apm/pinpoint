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
}
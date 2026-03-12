package com.navercorp.pinpoint.metric.collector.controller;

import com.navercorp.pinpoint.metric.common.model.Tag;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class TelegrafMetricControllerTest {

    @Test
    void isValidHostName_valid() {
        assertThat(TelegrafMetricController.isValidHostName("hostname")).isTrue();
        assertThat(TelegrafMetricController.isValidHostName("host-name")).isTrue();
        assertThat(TelegrafMetricController.isValidHostName("host1")).isTrue();
        assertThat(TelegrafMetricController.isValidHostName("a")).isTrue();
        assertThat(TelegrafMetricController.isValidHostName("a1")).isTrue();
        assertThat(TelegrafMetricController.isValidHostName("abc-123-def")).isTrue();
        assertThat(TelegrafMetricController.isValidHostName("host.name")).isTrue();
        assertThat(TelegrafMetricController.isValidHostName("host_name")).isTrue();
    }

    @Test
    void isValidHostName_invalid() {
        assertThat(TelegrafMetricController.isValidHostName("-hostname")).isFalse();
        assertThat(TelegrafMetricController.isValidHostName("hostname-")).isFalse();
        assertThat(TelegrafMetricController.isValidHostName("1hostname")).isFalse();
        assertThat(TelegrafMetricController.isValidHostName("HostName")).isFalse();
        assertThat(TelegrafMetricController.isValidHostName("host name")).isFalse();
        assertThat(TelegrafMetricController.isValidHostName("")).isFalse();
    }

    @Test
    void isValidHostName_exceedsMaxLabelLength() {
        String longName = "a".repeat(300);
        assertThat(TelegrafMetricController.isValidHostName(longName)).isFalse();
    }

    @Test
    void isValidGroupName_valid() {
        assertThat(TelegrafMetricController.isValidGroupName("group1")).isTrue();
        assertThat(TelegrafMetricController.isValidGroupName("Group-Name")).isTrue();
        assertThat(TelegrafMetricController.isValidGroupName("group.name")).isTrue();
        assertThat(TelegrafMetricController.isValidGroupName("Group.Name-01")).isTrue();
        assertThat(TelegrafMetricController.isValidGroupName("group_name")).isTrue();
        assertThat(TelegrafMetricController.isValidGroupName("A")).isTrue();
    }

    @Test
    void isValidGroupName_invalid() {
        assertThat(TelegrafMetricController.isValidGroupName("{groupname}")).isFalse();
        assertThat(TelegrafMetricController.isValidGroupName("-group")).isFalse();
        assertThat(TelegrafMetricController.isValidGroupName(".group")).isFalse();
        assertThat(TelegrafMetricController.isValidGroupName("group name")).isFalse();
        assertThat(TelegrafMetricController.isValidGroupName("")).isFalse();
    }

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
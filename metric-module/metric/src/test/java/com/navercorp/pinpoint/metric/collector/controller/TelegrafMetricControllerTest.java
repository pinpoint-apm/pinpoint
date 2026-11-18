package com.navercorp.pinpoint.metric.collector.controller;

import com.navercorp.pinpoint.common.server.util.json.Jackson;
import com.navercorp.pinpoint.metric.collector.model.TelegrafMetrics;
import com.navercorp.pinpoint.metric.collector.service.SystemMetricDataTypeService;
import com.navercorp.pinpoint.metric.collector.service.SystemMetricService;
import com.navercorp.pinpoint.metric.collector.service.SystemMetricTagService;
import com.navercorp.pinpoint.metric.common.model.Metrics;
import com.navercorp.pinpoint.metric.common.model.Tag;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

class TelegrafMetricControllerTest {

    private static final String METRICS_JSON = """
            {"metrics":[{"fields":{"usage_user":1.5},"name":"cpu","tags":{"cpu":"cpu0","host":"host1"},"timestamp":1700000000}]}
            """;

    private final SystemMetricService systemMetricService = mock(SystemMetricService.class);
    private final TelegrafMetricController controller = new TelegrafMetricController(systemMetricService,
            mock(SystemMetricDataTypeService.class), mock(SystemMetricTagService.class), () -> "tenantId");

    private ResponseEntity<String> save(String serviceName) throws Exception {
        TelegrafMetrics metrics = Jackson.newMapper().readValue(METRICS_JSON, TelegrafMetrics.class);
        return controller.saveSystemMetric("hostGroup", serviceName, metrics, mock(BindingResult.class));
    }

    private Metrics savedMetrics() {
        ArgumentCaptor<Metrics> captor = ArgumentCaptor.forClass(Metrics.class);
        verify(systemMetricService).insert(captor.capture());
        return captor.getValue();
    }

    @Test
    void saveSystemMetric_serviceName() throws Exception {
        assertThat(save("service1").getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(savedMetrics().getServiceName()).isEqualTo("service1");
    }

    @Test
    void saveSystemMetric_withoutServiceName_savesDefault() throws Exception {
        save(null);
        assertThat(savedMetrics().getServiceName()).isEqualTo("DEFAULT");
    }

    @Test
    void saveSystemMetric_emptyServiceName_savesDefault() throws Exception {
        save("");
        assertThat(savedMetrics().getServiceName()).isEqualTo("DEFAULT");
    }

    @Test
    void saveSystemMetric_lowerCaseDefault_savesDefault() throws Exception {
        save("default");
        assertThat(savedMetrics().getServiceName()).isEqualTo("DEFAULT");
    }

    @Test
    void saveSystemMetric_serviceNameLength() throws Exception {
        assertThat(save("a".repeat(254)).getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(save("a".repeat(255)).getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(save("ab").getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void saveSystemMetric_invalidServiceName() throws Exception {
        assertThat(save("service name").getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        verify(systemMetricService, never()).insert(any());
    }

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
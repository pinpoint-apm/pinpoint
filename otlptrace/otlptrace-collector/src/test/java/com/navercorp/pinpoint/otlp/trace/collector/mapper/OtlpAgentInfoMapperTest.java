package com.navercorp.pinpoint.otlp.trace.collector.mapper;

import com.navercorp.pinpoint.common.server.bo.AgentInfoBo;
import com.navercorp.pinpoint.common.server.bo.SpanBo;
import com.navercorp.pinpoint.common.server.bo.SpanOwner;
import com.navercorp.pinpoint.common.server.bo.TraceSourceType;
import com.navercorp.pinpoint.common.trace.ServiceType;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class OtlpAgentInfoMapperTest {

    @Test
    void map_registersApplicationLevelType_notSpanLevelSpecialization() {
        // A specialized root (e.g. a kafka CONSUMER as the first-seen span of an
        // application) must not decide the application's registered type.
        SpanBo spanBo = spanBo();
        spanBo.setServiceType(8660); // KAFKA_CLIENT — span-level specialization

        AgentInfoBo agentInfo = new OtlpAgentInfoMapper().map(spanBo, Map.of());

        assertThat(agentInfo.getServiceTypeCode()).isEqualTo(ServiceType.OPENTELEMETRY_SERVER.getCode());
    }

    @Test
    void map_basicIdentityFields() {
        SpanBo spanBo = spanBo();

        AgentInfoBo agentInfo = new OtlpAgentInfoMapper().map(spanBo, Map.of());

        assertThat(agentInfo.getAgentId()).isEqualTo("agent-1");
        assertThat(agentInfo.getApplicationName()).isEqualTo("app-1");
        assertThat(agentInfo.getServiceTypeCode()).isEqualTo(ServiceType.OPENTELEMETRY_SERVER.getCode());
    }

    private static SpanBo spanBo() {
        SpanOwner owner = new SpanOwner();
        owner.setAgentId("agent-1");
        owner.setApplicationName("app-1");
        owner.setAgentStartTime(1000L);
        SpanBo spanBo = new SpanBo(TraceSourceType.OPENTELEMETRY, owner);
        // mirrors OtlpTraceSpanMapper.map(): application-level type is always pinned
        spanBo.setApplicationServiceType(ServiceType.OPENTELEMETRY_SERVER.getCode());
        return spanBo;
    }
}

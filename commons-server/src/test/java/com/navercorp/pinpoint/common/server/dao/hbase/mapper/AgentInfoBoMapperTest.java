package com.navercorp.pinpoint.common.server.dao.hbase.mapper;

import com.navercorp.pinpoint.common.hbase.HbaseTables;
import com.navercorp.pinpoint.common.server.bo.AgentInfoBo;
import com.navercorp.pinpoint.common.server.bo.serializer.agent.AgentIdRowKeyEncoder;
import com.navercorp.pinpoint.common.server.uid.ServiceUid;
import org.apache.hadoop.hbase.Cell;
import org.apache.hadoop.hbase.KeyValue;
import org.apache.hadoop.hbase.client.Result;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class AgentInfoBoMapperTest {

    private final AgentIdRowKeyEncoder rowKeyEncoder = new AgentIdRowKeyEncoder();
    private final AgentInfoBoMapper mapper = new AgentInfoBoMapper();

    @Test
    public void serviceUidRoundTrip() throws Exception {
        AgentInfoBo written = newAgentInfo(ServiceUid.of(1234));

        AgentInfoBo read = mapper.mapRow(toResult(written, written.writeValue()), 0);

        assertThat(read.getServiceUid()).isEqualTo(ServiceUid.of(1234));
        assertThat(read.getAgentId()).isEqualTo("agentId");
        assertThat(read.getApplicationName()).isEqualTo("applicationName");
        assertThat(read.getAgentName()).isEqualTo("agentName");
    }

    @Test
    public void legacyValueWithoutServiceUidReadsAsDefault() throws Exception {
        AgentInfoBo written = newAgentInfo(ServiceUid.of(1234));
        byte[] value = written.writeValue();
        byte[] legacyValue = Arrays.copyOf(value, value.length - Integer.BYTES);

        AgentInfoBo read = mapper.mapRow(toResult(written, legacyValue), 0);

        assertThat(read.getServiceUid()).isEqualTo(ServiceUid.DEFAULT);
        assertThat(read.getAgentName()).isEqualTo("agentName");
    }

    private Result toResult(AgentInfoBo agentInfo, byte[] value) {
        byte[] rowKey = rowKeyEncoder.encodeRowKey(agentInfo.getAgentId(), agentInfo.getStartTime());
        HbaseTables.AgentInfo descriptor = HbaseTables.AGENTINFO_INFO;
        Cell cell = new KeyValue(rowKey, descriptor.getName(), descriptor.QUALIFIER_IDENTIFIER, value);
        return Result.create(List.of(cell));
    }

    private static AgentInfoBo newAgentInfo(ServiceUid serviceUid) {
        AgentInfoBo.Builder builder = new AgentInfoBo.Builder();
        builder.setAgentId("agentId");
        builder.setAgentName("agentName");
        builder.setApplicationName("applicationName");
        builder.setStartTime(1000L);
        builder.setServiceTypeCode(1000);
        builder.setServiceUid(serviceUid);
        return builder.build();
    }
}

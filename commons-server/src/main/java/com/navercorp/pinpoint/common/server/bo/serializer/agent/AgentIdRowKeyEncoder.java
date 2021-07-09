package com.navercorp.pinpoint.common.server.bo.serializer.agent;

import com.navercorp.pinpoint.common.hbase.HbaseTableConstants;

public class AgentIdRowKeyEncoder extends IdRowKeyEncoder {

    public AgentIdRowKeyEncoder() {
        super(HbaseTableConstants.AGENT_ID_MAX_LEN);
    }

    @Override
    public byte[] encodeRowKey(String agentId, long timestamp) {
        return super.encodeRowKey(agentId, timestamp);
    }
}

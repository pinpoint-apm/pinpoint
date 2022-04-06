package com.navercorp.pinpoint.web.dao.hbase.stat.v2;

import com.navercorp.pinpoint.common.server.bo.codec.stat.AgentStatDecoder;
import com.navercorp.pinpoint.common.server.bo.stat.AgentStatDataPoint;
import com.navercorp.pinpoint.common.server.bo.stat.AgentStatType;
import com.navercorp.pinpoint.common.server.util.time.Range;
import com.navercorp.pinpoint.web.dao.stat.AgentStatDao;
import com.navercorp.pinpoint.web.mapper.stat.AgentStatMapperV2;

import java.util.List;
import java.util.Objects;

public abstract class DefaultAgentStatDao<T extends AgentStatDataPoint> implements AgentStatDao<T> {
    private final AgentStatType statType;
    private final HbaseAgentStatDaoOperationsV2 operations;
    private final AgentStatDecoder<T> decoder;

    public DefaultAgentStatDao(AgentStatType statType, HbaseAgentStatDaoOperationsV2 operations, AgentStatDecoder<T> decoder) {
        this.statType = Objects.requireNonNull(statType, "statType");
        this.operations = Objects.requireNonNull(operations, "operations");
        this.decoder = Objects.requireNonNull(decoder, "decoder");
    }

    @Override
    public List<T> getAgentStatList(String agentId, Range range) {
        Objects.requireNonNull(agentId, "agentId");
        Objects.requireNonNull(range, "range");

        AgentStatMapperV2<T> mapper = operations.createRowMapper(decoder, range);
        return operations.getAgentStatList(statType, mapper, agentId, range);
    }

    @Override
    public boolean agentStatExists(String agentId, Range range) {
        Objects.requireNonNull(agentId, "agentId");
        Objects.requireNonNull(range, "range");

        AgentStatMapperV2<T> mapper = operations.createRowMapper(decoder, range);
        return operations.agentStatExists(statType, mapper, agentId, range);
    }
}

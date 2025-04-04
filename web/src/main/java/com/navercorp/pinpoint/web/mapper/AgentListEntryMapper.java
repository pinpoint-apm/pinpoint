package com.navercorp.pinpoint.web.mapper;

import com.navercorp.pinpoint.common.hbase.HbaseColumnFamily;
import com.navercorp.pinpoint.common.hbase.HbaseTables;
import com.navercorp.pinpoint.common.hbase.RowMapper;
import com.navercorp.pinpoint.common.hbase.util.CellUtils;
import com.navercorp.pinpoint.common.server.util.AgentListRowKeyUtils;
import com.navercorp.pinpoint.web.vo.agent.AgentListEntry;
import org.apache.hadoop.hbase.Cell;
import org.apache.hadoop.hbase.client.Result;
import org.springframework.stereotype.Component;

@Component
public class AgentListEntryMapper implements RowMapper<AgentListEntry> {

    private static final HbaseColumnFamily DESCRIPTOR = HbaseTables.AGENT_LIST;

    @Override
    public AgentListEntry mapRow(Result result, int rowNum) throws Exception {
        if (result.isEmpty()) {
            return null;
        }

        byte[] rowKey = result.getRow();
        String agentId = AgentListRowKeyUtils.getAgentId(rowKey);
        long agentStartTime = AgentListRowKeyUtils.getAgentStartTime(rowKey);

        Cell cell = result.getColumnLatestCell(DESCRIPTOR.getName(), DESCRIPTOR.getName());
        String agentName = CellUtils.valueToString(cell);

        return new AgentListEntry(agentId, agentName, agentStartTime);
    }
}

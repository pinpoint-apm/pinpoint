package com.navercorp.pinpoint.web.mapper;

import com.navercorp.pinpoint.common.hbase.RowMapper;
import com.navercorp.pinpoint.common.hbase.util.CellUtils;
import org.apache.hadoop.hbase.Cell;
import org.apache.hadoop.hbase.client.Result;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Component
public class AgentIdPerTimeMapper implements RowMapper<List<String>> {

    @Override
    public List<String> mapRow(Result result, int rowNum) throws Exception {
        if (result.isEmpty()) {
            return Collections.emptyList();
        }
        final Cell[] rawCells = result.rawCells();
        final List<String> agentIdList = new ArrayList<>(rawCells.length);

        for (Cell cell : rawCells) {
            final String agentId = CellUtils.qualifierToString(cell);
            agentIdList.add(agentId);
        }

        return agentIdList;
    }
}

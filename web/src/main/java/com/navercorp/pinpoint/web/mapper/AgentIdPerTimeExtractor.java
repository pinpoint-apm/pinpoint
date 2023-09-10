package com.navercorp.pinpoint.web.mapper;

import com.navercorp.pinpoint.common.hbase.ResultsExtractor;
import com.navercorp.pinpoint.common.hbase.RowMapper;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.client.ResultScanner;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

@Component
public class AgentIdPerTimeExtractor implements ResultsExtractor<List<String>> {
    private final RowMapper<List<String>> agentIdPerTimeMapper;

    public AgentIdPerTimeExtractor(@Qualifier("agentIdPerTimeMapper") RowMapper<List<String>> agentIdPerTimeMapper) {
        this.agentIdPerTimeMapper = Objects.requireNonNull(agentIdPerTimeMapper, "agentIdPerTimeMapper");
    }

    @Override
    public List<String> extractData(ResultScanner results) throws Exception {
        Set<String> agentIds = new HashSet<>();

        int rowNum = 0;
        for (Result result : results) {
            List<String> intermediateIds = agentIdPerTimeMapper.mapRow(result, rowNum++);
            if (!intermediateIds.isEmpty()) {
                agentIds.addAll(intermediateIds);
            }
        }
        return new ArrayList<>(agentIds);
    }
}

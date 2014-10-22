package com.nhn.pinpoint.web.mapper;

import com.nhn.pinpoint.common.PinpointConstants;
import com.nhn.pinpoint.common.bo.AgentInfoBo;
import com.nhn.pinpoint.common.util.BytesUtils;
import com.nhn.pinpoint.common.util.TimeUtils;
import org.apache.hadoop.hbase.KeyValue;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.util.Bytes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.hadoop.hbase.RowMapper;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author emeroad
 */
@Component
public class AgentInfoMapper implements RowMapper<List<AgentInfoBo>> {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Override
    public List<AgentInfoBo> mapRow(Result result, int rowNum) throws Exception {
        if (result.isEmpty()) {
            return Collections.emptyList();
        }
        KeyValue[] raw = result.raw();

        List<AgentInfoBo> agentInfoBoList = new ArrayList<AgentInfoBo>(raw.length);
        for (KeyValue keyValue : raw) {
            AgentInfoBo agentInfoBo = mappingAgentInfo(keyValue);

            agentInfoBoList.add(agentInfoBo);
        }

        return agentInfoBoList;
    }

    private AgentInfoBo mappingAgentInfo(KeyValue keyValue) {
        byte[] rowKey = keyValue.getRow();
        String agentId = Bytes.toString(rowKey, 0, PinpointConstants.AGENT_NAME_MAX_LEN - 1).trim();
        long reverseStartTime = BytesUtils.bytesToLong(rowKey, PinpointConstants.AGENT_NAME_MAX_LEN);
        long startTime = TimeUtils.recoveryTimeMillis(reverseStartTime);

        AgentInfoBo agentInfoBo = new AgentInfoBo.Builder(keyValue.getValue()).agentId(agentId).startTime(startTime).build();
        logger.debug("agentInfo:{}", agentInfoBo);
        return agentInfoBo;
    }
}

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
import java.util.List;

/**
 * @author emeroad
 */
@Component
public class AgentInfoMapper implements RowMapper<List<AgentInfoBo>> {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Override
    public List<AgentInfoBo> mapRow(Result result, int rowNum) throws Exception {
        KeyValue[] raw = result.raw();

        List<AgentInfoBo> agentInfoBoList = new ArrayList<AgentInfoBo>(raw.length);
        for (int i = 0; i < raw.length; i++) {
            KeyValue keyValue = raw[i];
            AgentInfoBo agentInfoBo = mappingAgentInfo(keyValue);

            agentInfoBoList.add(agentInfoBo);
        }

        return agentInfoBoList;
    }

    private AgentInfoBo mappingAgentInfo(KeyValue keyValue) {
        AgentInfoBo agentInfoBo = new AgentInfoBo();
        agentInfoBo.readValue(keyValue.getValue());

        byte[] rowKey = keyValue.getRow();
        String agentId = Bytes.toString(rowKey, 0, PinpointConstants.AGENT_NAME_MAX_LEN - 1).trim();
        agentInfoBo.setAgentId(agentId);

        long reverseStartTime = BytesUtils.bytesToLong(rowKey, PinpointConstants.AGENT_NAME_MAX_LEN);
        long startTime = TimeUtils.recoveryCurrentTimeMillis(reverseStartTime);
        agentInfoBo.setStartTime(startTime);

        logger.debug("agentInfo:{}", agentInfoBo);

        return agentInfoBo;
    }
}

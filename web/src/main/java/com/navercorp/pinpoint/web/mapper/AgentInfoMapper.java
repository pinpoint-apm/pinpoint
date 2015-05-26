/*
 * Copyright 2014 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.navercorp.pinpoint.web.mapper;

import com.navercorp.pinpoint.common.PinpointConstants;
import com.navercorp.pinpoint.common.bo.AgentInfoBo;
import com.navercorp.pinpoint.common.service.ServiceTypeRegistryService;
import com.navercorp.pinpoint.common.util.BytesUtils;
import com.navercorp.pinpoint.common.util.TimeUtils;

import org.apache.hadoop.hbase.KeyValue;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.util.Bytes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
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

    @Autowired
    private ServiceTypeRegistryService registry;

    @Override
    @SuppressWarnings("deprecation")
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

    @SuppressWarnings("deprecation")
    private AgentInfoBo mappingAgentInfo(KeyValue keyValue) {
        byte[] rowKey = keyValue.getRow();
        String agentId = Bytes.toString(rowKey, 0, PinpointConstants.AGENT_NAME_MAX_LEN - 1).trim();
        long reverseStartTime = BytesUtils.bytesToLong(rowKey, PinpointConstants.AGENT_NAME_MAX_LEN);
        long startTime = TimeUtils.recoveryTimeMillis(reverseStartTime);

        final AgentInfoBo.Builder builder = new AgentInfoBo.Builder(keyValue.getValue());
        builder.setAgentId(agentId);
        builder.setStartTime(startTime);
        // TODO fix
        builder.setServiceType(registry.findServiceType(builder.getServiceTypeCode()));
        AgentInfoBo agentInfoBo = builder.build();
        logger.debug("agentInfo:{}", agentInfoBo);
        return agentInfoBo;
    }
}

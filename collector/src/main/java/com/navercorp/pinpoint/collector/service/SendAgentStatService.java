/*
 * Copyright 2017 NAVER Corp.
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
package com.navercorp.pinpoint.collector.service;

import com.navercorp.pinpoint.collector.config.FlinkConfiguration;
import com.navercorp.pinpoint.collector.mapper.thrift.stat.TFAgentStatBatchMapper;
import com.navercorp.pinpoint.common.server.bo.stat.AgentStatBo;
import com.navercorp.pinpoint.thrift.dto.flink.TFAgentStatBatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * @author minwoo.jung
 */
@Service("sendAgentStatService")
public class SendAgentStatService extends SendDataToFlinkService implements AgentStatService {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private final boolean flinkClusterEnable;
    private final TFAgentStatBatchMapper tFAgentStatBatchMapper = new TFAgentStatBatchMapper();

    public SendAgentStatService(FlinkConfiguration config) {
        this.flinkClusterEnable = config.isFlinkClusterEnable();
    }

    @Override
    public void save(AgentStatBo agentStatBo) {
        if (!flinkClusterEnable) {
            return;
        }

        TFAgentStatBatch tFAgentStatBatch = tFAgentStatBatchMapper.map(agentStatBo);
        sendData(tFAgentStatBatch);
    }
}

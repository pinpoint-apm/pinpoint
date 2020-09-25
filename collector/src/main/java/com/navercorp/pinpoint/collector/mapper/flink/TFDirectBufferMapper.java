/*
 * Copyright 2018 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.navercorp.pinpoint.collector.mapper.flink;

import com.navercorp.pinpoint.common.server.bo.stat.AgentStatBo;
import com.navercorp.pinpoint.common.server.bo.stat.DirectBufferBo;
import com.navercorp.pinpoint.thrift.dto.flink.TFAgentStat;
import com.navercorp.pinpoint.thrift.dto.flink.TFDirectBuffer;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @author Roy Kim
 */
@Component
public class TFDirectBufferMapper implements FlinkStatMapper<DirectBufferBo, TFAgentStat> {

    public TFDirectBuffer map(DirectBufferBo directBufferBo) {
        TFDirectBuffer tfFDirectBuffer = new TFDirectBuffer();
        tfFDirectBuffer.setDirectCount(directBufferBo.getDirectCount());
        tfFDirectBuffer.setDirectMemoryUsed(directBufferBo.getDirectMemoryUsed());
        tfFDirectBuffer.setMappedCount(directBufferBo.getMappedCount());
        tfFDirectBuffer.setMappedMemoryUsed(directBufferBo.getMappedMemoryUsed());
        return tfFDirectBuffer;
    }

    @Override
    public void map(DirectBufferBo directBufferBo, TFAgentStat tfAgentStat) {
        tfAgentStat.setDirectBuffer(map(directBufferBo));
    }

    @Override
    public void build(TFAgentStatMapper.TFAgentStatBuilder builder) {
        AgentStatBo agentStat = builder.getAgentStat();
        List<DirectBufferBo> directBufferList = agentStat.getDirectBufferBos();
        builder.build(directBufferList, this);
    }
}

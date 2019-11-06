/*
 * Copyright 2018 Naver Corp.
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

package com.navercorp.pinpoint.flink.mapper.thrift.stat;

import com.navercorp.pinpoint.common.server.bo.stat.join.JoinFileDescriptorBo;
import com.navercorp.pinpoint.flink.mapper.thrift.ThriftBoMapper;
import com.navercorp.pinpoint.thrift.dto.flink.TFAgentStat;
import com.navercorp.pinpoint.thrift.dto.flink.TFFileDescriptor;

/**
 * @author Roy Kim
 */
public class JoinFileDescriptorBoMapper implements ThriftBoMapper<JoinFileDescriptorBo, TFAgentStat> {

    @Override
    public JoinFileDescriptorBo map(TFAgentStat tFAgentStat) {
        if (!tFAgentStat.isSetFileDescriptor()) {
            return JoinFileDescriptorBo.EMPTY_JOIN_FILE_DESCRIPTOR_BO;
        }

        JoinFileDescriptorBo joinFileDescriptorBo = new JoinFileDescriptorBo();

        final String agentId = tFAgentStat.getAgentId();
        joinFileDescriptorBo.setId(agentId);
        joinFileDescriptorBo.setTimestamp(tFAgentStat.getTimestamp());

        TFFileDescriptor tFFileDescriptor = tFAgentStat.getFileDescriptor();
        joinFileDescriptorBo.setAvgOpenFDCount(tFFileDescriptor.getOpenFileDescriptorCount());
        joinFileDescriptorBo.setMinOpenFDCountAgentId(agentId);
        joinFileDescriptorBo.setMinOpenFDCount(tFFileDescriptor.getOpenFileDescriptorCount());
        joinFileDescriptorBo.setMaxOpenFDCountAgentId(agentId);
        joinFileDescriptorBo.setMaxOpenFDCount(tFFileDescriptor.getOpenFileDescriptorCount());

        return joinFileDescriptorBo;
    }
}

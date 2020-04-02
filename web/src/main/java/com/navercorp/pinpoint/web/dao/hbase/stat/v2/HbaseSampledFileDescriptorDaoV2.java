/*
 * Copyright 2016 Naver Corp.
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

package com.navercorp.pinpoint.web.dao.hbase.stat.v2;

import com.navercorp.pinpoint.common.server.bo.codec.stat.FileDescriptorDecoder;
import com.navercorp.pinpoint.common.server.bo.stat.AgentStatType;
import com.navercorp.pinpoint.common.server.bo.stat.FileDescriptorBo;
import com.navercorp.pinpoint.web.dao.stat.SampledFileDescriptorDao;
import com.navercorp.pinpoint.web.mapper.stat.AgentStatMapperV2;
import com.navercorp.pinpoint.web.mapper.stat.SampledAgentStatResultExtractor;
import com.navercorp.pinpoint.web.mapper.stat.sampling.sampler.FileDescriptorSampler;
import com.navercorp.pinpoint.web.util.TimeWindow;
import com.navercorp.pinpoint.web.vo.Range;
import com.navercorp.pinpoint.web.vo.stat.SampledFileDescriptor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Objects;

/**
 * @author Roy Kim
 */
@Repository("sampledFileDescriptorDaoV2")
public class HbaseSampledFileDescriptorDaoV2 implements SampledFileDescriptorDao {

    private final HbaseAgentStatDaoOperationsV2 operations;

    private final FileDescriptorDecoder fileDescriptorDecoder;
    private final FileDescriptorSampler fileDescriptorSampler;

    public HbaseSampledFileDescriptorDaoV2(HbaseAgentStatDaoOperationsV2 operations, FileDescriptorDecoder fileDescriptorDecoder, FileDescriptorSampler fileDescriptorSampler) {
        this.operations = Objects.requireNonNull(operations, "operations");
        this.fileDescriptorDecoder = Objects.requireNonNull(fileDescriptorDecoder, "fileDescriptorDecoder");
        this.fileDescriptorSampler = Objects.requireNonNull(fileDescriptorSampler, "fileDescriptorSampler");
    }

    @Override
    public List<SampledFileDescriptor> getSampledAgentStatList(String agentId, TimeWindow timeWindow) {
        long scanFrom = timeWindow.getWindowRange().getFrom();
        long scanTo = timeWindow.getWindowRange().getTo() + timeWindow.getWindowSlotSize();
        Range range = new Range(scanFrom, scanTo);
        AgentStatMapperV2<FileDescriptorBo> mapper = operations.createRowMapper(fileDescriptorDecoder, range);
        SampledAgentStatResultExtractor<FileDescriptorBo, SampledFileDescriptor> resultExtractor = new SampledAgentStatResultExtractor<>(timeWindow, mapper, fileDescriptorSampler);
        return operations.getSampledAgentStatList(AgentStatType.FILE_DESCRIPTOR, resultExtractor, agentId, range);
    }
}

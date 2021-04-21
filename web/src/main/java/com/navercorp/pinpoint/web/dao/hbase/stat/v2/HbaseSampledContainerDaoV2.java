/*
 * Copyright 2020 NAVER Corp.
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

import com.navercorp.pinpoint.common.server.bo.codec.stat.ContainerDecoder;
import com.navercorp.pinpoint.common.server.bo.stat.AgentStatType;
import com.navercorp.pinpoint.common.server.bo.stat.ContainerBo;
import com.navercorp.pinpoint.web.dao.stat.SampledContainerDao;
import com.navercorp.pinpoint.web.mapper.stat.AgentStatMapperV2;
import com.navercorp.pinpoint.web.mapper.stat.SampledAgentStatResultExtractor;
import com.navercorp.pinpoint.web.mapper.stat.sampling.sampler.ContainerSampler;
import com.navercorp.pinpoint.web.util.TimeWindow;
import com.navercorp.pinpoint.web.vo.Range;
import com.navercorp.pinpoint.web.vo.stat.SampledContainer;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Objects;

/**
 * @author Hyunjoon Cho
 */
@Repository("sampledContainerDaoV2")
public class HbaseSampledContainerDaoV2 implements SampledContainerDao {

    private final HbaseAgentStatDaoOperationsV2 operations;

    private final ContainerDecoder containerDecoder;
    private final ContainerSampler containerSampler;

    public HbaseSampledContainerDaoV2(HbaseAgentStatDaoOperationsV2 operations, ContainerDecoder containerDecoder, ContainerSampler containerSampler) {
        this.operations = Objects.requireNonNull(operations, "operations");
        this.containerDecoder = Objects.requireNonNull(containerDecoder, "containerDecoder");
        this.containerSampler = Objects.requireNonNull(containerSampler, "containerSampler");
    }

    @Override
    public List<SampledContainer> getSampledAgentStatList(String agentId, TimeWindow timeWindow) {
        long scanFrom = timeWindow.getWindowRange().getFrom();
        long scanTo = timeWindow.getWindowRange().getTo() + timeWindow.getWindowSlotSize();
        Range range = Range.newRange(scanFrom, scanTo);
        AgentStatMapperV2<ContainerBo> mapper = operations.createRowMapper(containerDecoder, range);
        SampledAgentStatResultExtractor<ContainerBo, SampledContainer> resultExtractor = new SampledAgentStatResultExtractor<>(timeWindow, mapper, containerSampler);
        return operations.getSampledAgentStatList(AgentStatType.CONTAINER, resultExtractor, agentId, range);
    }
}

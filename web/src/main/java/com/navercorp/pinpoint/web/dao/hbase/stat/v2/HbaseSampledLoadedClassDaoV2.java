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

import com.navercorp.pinpoint.common.server.bo.codec.stat.LoadedClassCountDecoder;
import com.navercorp.pinpoint.common.server.bo.stat.AgentStatType;
import com.navercorp.pinpoint.common.server.bo.stat.LoadedClassBo;
import com.navercorp.pinpoint.web.dao.stat.SampledLoadedClassCountDao;
import com.navercorp.pinpoint.web.mapper.stat.AgentStatMapperV2;
import com.navercorp.pinpoint.web.mapper.stat.SampledAgentStatResultExtractor;
import com.navercorp.pinpoint.web.mapper.stat.sampling.sampler.LoadedClassSampler;
import com.navercorp.pinpoint.web.util.TimeWindow;
import com.navercorp.pinpoint.web.vo.Range;
import com.navercorp.pinpoint.web.vo.stat.SampledLoadedClassCount;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Objects;

@Repository("sampledLoadedClassDaoV2")
public class HbaseSampledLoadedClassDaoV2 implements SampledLoadedClassCountDao {
    private final HbaseAgentStatDaoOperationsV2 operations;
    private final LoadedClassCountDecoder loadedClassDecoder;
    private final LoadedClassSampler loadedClassSampler;

    public HbaseSampledLoadedClassDaoV2(HbaseAgentStatDaoOperationsV2 operations, LoadedClassCountDecoder loadedClassDecoder, LoadedClassSampler loadedClassSampler) {
        this.operations = Objects.requireNonNull(operations, "operations");
        this.loadedClassDecoder = Objects.requireNonNull(loadedClassDecoder, "loadedClassDecoder");
        this.loadedClassSampler = Objects.requireNonNull(loadedClassSampler, "loadedClassSampler");
    }

    @Override
    public List<SampledLoadedClassCount> getSampledAgentStatList(String agentId, TimeWindow timeWindow) {
        long scanFrom = timeWindow.getWindowRange().getFrom();
        long scanTo = timeWindow.getWindowRange().getTo() + timeWindow.getWindowSlotSize();
        Range range = Range.newRange(scanFrom, scanTo);
        AgentStatMapperV2<LoadedClassBo> mapper = operations.createRowMapper(loadedClassDecoder, range);
        SampledAgentStatResultExtractor<LoadedClassBo, SampledLoadedClassCount> resultExtractor = new SampledAgentStatResultExtractor<>(timeWindow, mapper, loadedClassSampler);
        return operations.getSampledAgentStatList(AgentStatType.LOADED_CLASS, resultExtractor, agentId, range);
    }
}

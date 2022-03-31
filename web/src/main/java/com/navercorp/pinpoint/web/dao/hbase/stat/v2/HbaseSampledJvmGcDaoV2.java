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

import com.navercorp.pinpoint.common.server.bo.codec.stat.JvmGcDecoder;
import com.navercorp.pinpoint.common.server.bo.stat.AgentStatType;
import com.navercorp.pinpoint.common.server.bo.stat.JvmGcBo;
import com.navercorp.pinpoint.web.dao.stat.SampledJvmGcDao;
import com.navercorp.pinpoint.web.mapper.stat.AgentStatMapperV2;
import com.navercorp.pinpoint.web.mapper.stat.sampling.sampler.JvmGcSampler;
import com.navercorp.pinpoint.web.mapper.stat.SampledAgentStatResultExtractor;
import com.navercorp.pinpoint.web.util.TimeWindow;
import com.navercorp.pinpoint.common.server.util.time.Range;
import com.navercorp.pinpoint.web.vo.stat.SampledJvmGc;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Objects;

/**
 * @author HyunGil Jeong
 */
@Repository("sampledJvmGcDaoV2")
public class HbaseSampledJvmGcDaoV2 implements SampledJvmGcDao {

    private final HbaseAgentStatDaoOperationsV2 operations;

    private final JvmGcDecoder jvmGcDecoder;
    private final JvmGcSampler jvmGcSampler;

    public HbaseSampledJvmGcDaoV2(HbaseAgentStatDaoOperationsV2 operations, JvmGcDecoder jvmGcDecoder, JvmGcSampler jvmGcSampler) {
        this.operations = Objects.requireNonNull(operations, "operations");
        this.jvmGcDecoder = Objects.requireNonNull(jvmGcDecoder, "jvmGcDecoder");
        this.jvmGcSampler = Objects.requireNonNull(jvmGcSampler, "jvmGcSampler");
    }

    @Override
    public List<SampledJvmGc> getSampledAgentStatList(String agentId, TimeWindow timeWindow) {
        Range range = timeWindow.getWindowSlotRange();

        AgentStatMapperV2<JvmGcBo> mapper = operations.createRowMapper(jvmGcDecoder, range);
        SampledAgentStatResultExtractor<JvmGcBo, SampledJvmGc> resultExtractor = new SampledAgentStatResultExtractor<>(timeWindow, mapper, jvmGcSampler);
        return operations.getSampledAgentStatList(AgentStatType.JVM_GC, resultExtractor, agentId, range);
    }
}

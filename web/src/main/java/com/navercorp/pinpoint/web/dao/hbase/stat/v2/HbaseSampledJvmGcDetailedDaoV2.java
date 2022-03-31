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

import com.navercorp.pinpoint.common.server.bo.codec.stat.JvmGcDetailedDecoder;
import com.navercorp.pinpoint.common.server.bo.stat.AgentStatType;
import com.navercorp.pinpoint.common.server.bo.stat.JvmGcDetailedBo;
import com.navercorp.pinpoint.web.dao.stat.SampledJvmGcDetailedDao;
import com.navercorp.pinpoint.web.mapper.stat.AgentStatMapperV2;
import com.navercorp.pinpoint.web.mapper.stat.SampledAgentStatResultExtractor;
import com.navercorp.pinpoint.web.mapper.stat.sampling.sampler.JvmGcDetailedSampler;
import com.navercorp.pinpoint.web.util.TimeWindow;
import com.navercorp.pinpoint.common.server.util.time.Range;
import com.navercorp.pinpoint.web.vo.stat.SampledJvmGcDetailed;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Objects;

/**
 * @author HyunGil Jeong
 */
@Repository("sampledJvmGcDetailedDaoV2")
public class HbaseSampledJvmGcDetailedDaoV2 implements SampledJvmGcDetailedDao {

    private final HbaseAgentStatDaoOperationsV2 operations;

    private final JvmGcDetailedDecoder jvmGcDetailedDecoder;
    private final JvmGcDetailedSampler jvmGcDetailedSampler;

    public HbaseSampledJvmGcDetailedDaoV2(HbaseAgentStatDaoOperationsV2 operations, JvmGcDetailedDecoder jvmGcDetailedDecoder, JvmGcDetailedSampler jvmGcDetailedSampler) {
        this.operations = Objects.requireNonNull(operations, "operations");
        this.jvmGcDetailedDecoder = Objects.requireNonNull(jvmGcDetailedDecoder, "jvmGcDetailedDecoder");
        this.jvmGcDetailedSampler = Objects.requireNonNull(jvmGcDetailedSampler, "jvmGcDetailedSampler");
    }

    @Override
    public List<SampledJvmGcDetailed> getSampledAgentStatList(String agentId, TimeWindow timeWindow) {
        Range range = timeWindow.getWindowSlotRange();

        AgentStatMapperV2<JvmGcDetailedBo> mapper = operations.createRowMapper(jvmGcDetailedDecoder, range);
        SampledAgentStatResultExtractor<JvmGcDetailedBo, SampledJvmGcDetailed> resultExtractor = new SampledAgentStatResultExtractor<>(timeWindow, mapper, jvmGcDetailedSampler);
        return operations.getSampledAgentStatList(AgentStatType.JVM_GC_DETAILED, resultExtractor, agentId, range);
    }
}

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

import com.navercorp.pinpoint.common.server.bo.codec.stat.TotalThreadCountDecoder;
import com.navercorp.pinpoint.common.server.bo.stat.AgentStatType;
import com.navercorp.pinpoint.common.server.bo.stat.TotalThreadCountBo;
import com.navercorp.pinpoint.web.dao.stat.SampledTotalThreadCountDao;
import com.navercorp.pinpoint.web.mapper.stat.AgentStatMapperV2;
import com.navercorp.pinpoint.web.mapper.stat.SampledAgentStatResultExtractor;
import com.navercorp.pinpoint.web.mapper.stat.sampling.sampler.TotalThreadCountSampler;
import com.navercorp.pinpoint.web.util.TimeWindow;
import com.navercorp.pinpoint.web.vo.Range;
import com.navercorp.pinpoint.web.vo.stat.SampledTotalThreadCount;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Objects;


@Repository("sampledTotalThreadCountDaoV2")
public class HBaseSampledTotalThreadCountDaoV2 implements SampledTotalThreadCountDao {
    private final HbaseAgentStatDaoOperationsV2 operations;

    private final TotalThreadCountDecoder totalThreadCountDecoder;
    private final TotalThreadCountSampler totalThreadCountSampler;

    public HBaseSampledTotalThreadCountDaoV2(HbaseAgentStatDaoOperationsV2 operations, TotalThreadCountDecoder totalThreadCountDecoder, TotalThreadCountSampler totalThreadCountSampler) {
        this.operations = Objects.requireNonNull(operations, "operations");
        this.totalThreadCountDecoder = Objects.requireNonNull(totalThreadCountDecoder, "totalThreadCountDecoder");
        this.totalThreadCountSampler = Objects.requireNonNull(totalThreadCountSampler, "totalThreadCountSampler");
    }

    @Override
    public List<SampledTotalThreadCount> getSampledAgentStatList(String agentId, TimeWindow timeWindow) {
        long scanFrom = timeWindow.getWindowRange().getFrom();
        long scanTo = timeWindow.getWindowRange().getTo() + timeWindow.getWindowSlotSize();
        Range range = Range.newRange(scanFrom, scanTo);
        AgentStatMapperV2<TotalThreadCountBo> mapper = operations.createRowMapper(totalThreadCountDecoder, range);

        SampledAgentStatResultExtractor<TotalThreadCountBo, SampledTotalThreadCount> resultExtractor = new SampledAgentStatResultExtractor<>(timeWindow, mapper, totalThreadCountSampler);
        return operations.getSampledAgentStatList(AgentStatType.TOTAL_THREAD, resultExtractor, agentId, range);
    }
}

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

package com.navercorp.pinpoint.web.dao.hbase.stat.v2;

import com.navercorp.pinpoint.common.server.bo.codec.stat.ResponseTimeDecoder;
import com.navercorp.pinpoint.common.server.bo.stat.AgentStatType;
import com.navercorp.pinpoint.common.server.bo.stat.ResponseTimeBo;
import com.navercorp.pinpoint.web.dao.stat.SampledResponseTimeDao;
import com.navercorp.pinpoint.web.mapper.stat.AgentStatMapperV2;
import com.navercorp.pinpoint.web.mapper.stat.SampledAgentStatResultExtractor;
import com.navercorp.pinpoint.web.mapper.stat.sampling.sampler.ResponseTimeSampler;
import com.navercorp.pinpoint.web.util.TimeWindow;
import com.navercorp.pinpoint.web.vo.Range;
import com.navercorp.pinpoint.web.vo.stat.SampledResponseTime;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Objects;

/**
 * @author Taejin Koo
 */
@Repository("sampledResponseTimeDaoV2")
public class HbaseSampledResponseTimeDaoV2 implements SampledResponseTimeDao {

    private final HbaseAgentStatDaoOperationsV2 operations;

    private final ResponseTimeDecoder responseTimeDecoder;
    private final ResponseTimeSampler responseTimeSampler;

    public HbaseSampledResponseTimeDaoV2(HbaseAgentStatDaoOperationsV2 operations, ResponseTimeDecoder responseTimeDecoder, ResponseTimeSampler responseTimeSampler) {
        this.operations = Objects.requireNonNull(operations, "operations");
        this.responseTimeDecoder = Objects.requireNonNull(responseTimeDecoder, "responseTimeDecoder");
        this.responseTimeSampler = Objects.requireNonNull(responseTimeSampler, "responseTimeSampler");
    }

    @Override
    public List<SampledResponseTime> getSampledAgentStatList(String agentId, TimeWindow timeWindow) {
        long scanFrom = timeWindow.getWindowRange().getFrom();
        long scanTo = timeWindow.getWindowRange().getTo() + timeWindow.getWindowSlotSize();
        Range range = new Range(scanFrom, scanTo);
        AgentStatMapperV2<ResponseTimeBo> mapper = operations.createRowMapper(responseTimeDecoder, range);

        SampledAgentStatResultExtractor<ResponseTimeBo, SampledResponseTime> resultExtractor = new SampledAgentStatResultExtractor<>(timeWindow, mapper, responseTimeSampler);
        return operations.getSampledAgentStatList(AgentStatType.RESPONSE_TIME, resultExtractor, agentId, range);
    }

}

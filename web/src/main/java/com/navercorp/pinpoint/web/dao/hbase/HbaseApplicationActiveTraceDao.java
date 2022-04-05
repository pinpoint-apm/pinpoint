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
package com.navercorp.pinpoint.web.dao.hbase;

import com.navercorp.pinpoint.common.hbase.ResultsExtractor;
import com.navercorp.pinpoint.common.hbase.RowMapper;
import com.navercorp.pinpoint.common.server.bo.codec.stat.ApplicationStatDecoder;
import com.navercorp.pinpoint.common.server.bo.stat.join.JoinActiveTraceBo;
import com.navercorp.pinpoint.common.server.bo.stat.join.StatType;
import com.navercorp.pinpoint.web.dao.ApplicationActiveTraceDao;
import com.navercorp.pinpoint.web.mapper.stat.SampledApplicationStatResultExtractor;
import com.navercorp.pinpoint.web.mapper.stat.sampling.sampler.ApplicationStatSampler;
import com.navercorp.pinpoint.web.util.TimeWindow;
import com.navercorp.pinpoint.common.server.util.time.Range;
import com.navercorp.pinpoint.web.vo.stat.AggreJoinActiveTraceBo;

import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Objects;

/**
 * @author minwoo.jung
 */
@Repository
public class HbaseApplicationActiveTraceDao implements ApplicationActiveTraceDao {

    private final ApplicationStatDecoder<JoinActiveTraceBo> activeTraceDecoder;

    private final ApplicationStatSampler<JoinActiveTraceBo, AggreJoinActiveTraceBo> activeTraceSampler;

    private final HbaseApplicationStatDaoOperations operations;

    public HbaseApplicationActiveTraceDao(ApplicationStatDecoder<JoinActiveTraceBo> activeTraceDecoder,
                                          ApplicationStatSampler<JoinActiveTraceBo, AggreJoinActiveTraceBo> activeTraceSampler,
                                          HbaseApplicationStatDaoOperations operations) {
        this.activeTraceDecoder = Objects.requireNonNull(activeTraceDecoder, "activeTraceDecoder");
        this.activeTraceSampler = Objects.requireNonNull(activeTraceSampler, "activeTraceSampler");
        this.operations = Objects.requireNonNull(operations, "operations");
    }

    @Override
    public List<AggreJoinActiveTraceBo> getApplicationStatList(String applicationId, TimeWindow timeWindow) {
        Range range = timeWindow.getWindowSlotRange();

        RowMapper<List<JoinActiveTraceBo>> mapper = operations.createRowMapper(activeTraceDecoder, range);
        ResultsExtractor<List<AggreJoinActiveTraceBo>> resultExtractor = new SampledApplicationStatResultExtractor<>(timeWindow, mapper, activeTraceSampler);
        return operations.getSampledStatList(StatType.APP_ACTIVE_TRACE_COUNT, resultExtractor, applicationId, range);
    }

}

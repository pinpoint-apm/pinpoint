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
import com.navercorp.pinpoint.common.server.bo.stat.join.JoinResponseTimeBo;
import com.navercorp.pinpoint.common.server.bo.stat.join.StatType;
import com.navercorp.pinpoint.web.dao.ApplicationResponseTimeDao;
import com.navercorp.pinpoint.web.mapper.stat.SampledApplicationStatResultExtractor;
import com.navercorp.pinpoint.web.mapper.stat.sampling.sampler.ApplicationStatSampler;
import com.navercorp.pinpoint.web.util.TimeWindow;
import com.navercorp.pinpoint.common.server.util.time.Range;
import com.navercorp.pinpoint.web.vo.stat.AggreJoinResponseTimeBo;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Objects;

/**
 * @author minwoo.jung
 */
@Repository
public class HbaseApplicationResponseTimeDao implements ApplicationResponseTimeDao {

    private final ApplicationStatDecoder<JoinResponseTimeBo> responseTimeDecoder;

    private final ApplicationStatSampler<JoinResponseTimeBo, AggreJoinResponseTimeBo> joinResponseTimeSampler;

    private final HbaseApplicationStatDaoOperations operations;

    public HbaseApplicationResponseTimeDao(ApplicationStatDecoder<JoinResponseTimeBo> responseTimeDecoder,
                                           ApplicationStatSampler<JoinResponseTimeBo, AggreJoinResponseTimeBo> joinResponseTimeSampler,
                                           HbaseApplicationStatDaoOperations operations) {
        this.responseTimeDecoder = Objects.requireNonNull(responseTimeDecoder, "responseTimeDecoder");
        this.joinResponseTimeSampler = Objects.requireNonNull(joinResponseTimeSampler, "joinResponseTimeSampler");
        this.operations = Objects.requireNonNull(operations, "operations");
    }

    @Override
    public List<AggreJoinResponseTimeBo> getApplicationStatList(String applicationId, TimeWindow timeWindow) {
        Range range = timeWindow.getWindowSlotRange();

        RowMapper<List<JoinResponseTimeBo>> mapper = operations.createRowMapper(responseTimeDecoder, range);
        ResultsExtractor<List<AggreJoinResponseTimeBo>> resultExtractor = new SampledApplicationStatResultExtractor<>(timeWindow, mapper, joinResponseTimeSampler);
        return operations.getSampledStatList(StatType.APP_RESPONSE_TIME, resultExtractor, applicationId, range);
    }

}

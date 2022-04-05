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
import com.navercorp.pinpoint.common.server.bo.stat.join.JoinStatBo;
import com.navercorp.pinpoint.common.server.bo.stat.join.StatType;
import com.navercorp.pinpoint.common.server.util.time.Range;
import com.navercorp.pinpoint.web.dao.appmetric.ApplicationMetricDao;
import com.navercorp.pinpoint.web.mapper.stat.SampledApplicationStatResultExtractor;
import com.navercorp.pinpoint.web.mapper.stat.sampling.sampler.ApplicationStatSampler;
import com.navercorp.pinpoint.web.util.TimeWindow;
import com.navercorp.pinpoint.web.vo.stat.AggregationStatData;

import java.util.List;
import java.util.Objects;

/**
 * @author minwoo.jung
 */
public class DefaultAgentMetricDao<IN extends JoinStatBo, OUT extends AggregationStatData> implements ApplicationMetricDao<OUT> {

    private final StatType statType;
    private final ApplicationStatDecoder<IN> decoder;

    private final ApplicationStatSampler<IN, OUT> sampler;

    private final HbaseApplicationStatDaoOperations operations;

    public DefaultAgentMetricDao(StatType statType,
                                 ApplicationStatDecoder<IN> decoder,
                                 ApplicationStatSampler<IN, OUT> sampler,
                                 HbaseApplicationStatDaoOperations operations) {
        this.statType = Objects.requireNonNull(statType, "statType");
        this.decoder = Objects.requireNonNull(decoder, "decoder");
        this.sampler = Objects.requireNonNull(sampler, "sampler");
        this.operations = Objects.requireNonNull(operations, "operations");
    }

    @Override
    public List<OUT> getApplicationStatList(String applicationId, TimeWindow timeWindow) {
        Range range = timeWindow.getWindowSlotRange();

        RowMapper<List<IN>> mapper = operations.createRowMapper(decoder, range);
        ResultsExtractor<List<OUT>> resultExtractor = new SampledApplicationStatResultExtractor<>(timeWindow, mapper, sampler);
        return operations.getSampledStatList(statType, resultExtractor, applicationId, range);
    }

}

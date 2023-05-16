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

package com.navercorp.pinpoint.web.dao.hbase.stat;

import com.navercorp.pinpoint.common.hbase.ResultsExtractor;
import com.navercorp.pinpoint.common.server.bo.codec.stat.AgentStatDecoder;
import com.navercorp.pinpoint.common.server.bo.stat.AgentStatType;
import com.navercorp.pinpoint.common.server.bo.stat.DataSourceBo;
import com.navercorp.pinpoint.common.server.bo.stat.DataSourceListBo;
import com.navercorp.pinpoint.common.server.util.time.Range;
import com.navercorp.pinpoint.web.dao.SampledAgentStatDao;
import com.navercorp.pinpoint.web.mapper.stat.AgentStatMapperV2;
import com.navercorp.pinpoint.web.mapper.stat.SampledDataSourceResultExtractor;
import com.navercorp.pinpoint.web.mapper.stat.sampling.sampler.AgentStatSampler;
import com.navercorp.pinpoint.web.util.TimeWindow;
import com.navercorp.pinpoint.web.vo.stat.SampledDataSource;
import com.navercorp.pinpoint.web.vo.stat.SampledDataSourceList;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Objects;

/**
 * @author Taejin Koo
 */
@Repository
public class HbaseSampledDataSourceDao implements SampledAgentStatDao<SampledDataSourceList> {

    private final AgentStatType statType = AgentStatType.DATASOURCE;
    private final HbaseAgentStatDaoOperations operations;

    private final AgentStatDecoder<DataSourceListBo> decoder;
    private final AgentStatSampler<DataSourceBo, SampledDataSource> sampler;

    public HbaseSampledDataSourceDao(HbaseAgentStatDaoOperations operations,
                                     AgentStatDecoder<DataSourceListBo> decoder,
                                     AgentStatSampler<DataSourceBo, SampledDataSource> sampler) {
        this.operations = Objects.requireNonNull(operations, "operations");
        this.decoder = Objects.requireNonNull(decoder, "decoder");
        this.sampler = Objects.requireNonNull(sampler, "sampler");
    }

    @Override
    public List<SampledDataSourceList> getSampledAgentStatList(String agentId, TimeWindow timeWindow) {
        Range range = timeWindow.getWindowSlotRange();

        AgentStatMapperV2<DataSourceListBo> mapper = operations.createRowMapper(decoder, range);
        ResultsExtractor<List<SampledDataSourceList>> resultExtractor = new SampledDataSourceResultExtractor(timeWindow, mapper, sampler);
        return operations.getSampledAgentStatList(statType, resultExtractor, agentId, range);
    }

    @Override
    public String getChartType() {
        return statType.getChartType();
    }
}

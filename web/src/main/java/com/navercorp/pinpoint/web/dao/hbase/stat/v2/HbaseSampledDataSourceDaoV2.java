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

import com.navercorp.pinpoint.common.server.bo.codec.stat.DataSourceDecoder;
import com.navercorp.pinpoint.common.server.bo.stat.AgentStatType;
import com.navercorp.pinpoint.common.server.bo.stat.DataSourceListBo;
import com.navercorp.pinpoint.web.dao.stat.SampledDataSourceDao;
import com.navercorp.pinpoint.web.mapper.stat.AgentStatMapperV2;
import com.navercorp.pinpoint.web.mapper.stat.SampledDataSourceResultExtractor;
import com.navercorp.pinpoint.web.mapper.stat.sampling.sampler.DataSourceSampler;
import com.navercorp.pinpoint.web.util.TimeWindow;
import com.navercorp.pinpoint.web.vo.Range;
import com.navercorp.pinpoint.web.vo.stat.SampledDataSourceList;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Objects;

/**
 * @author Taejin Koo
 */
@Repository("sampledDataSourceDaoV2")
public class HbaseSampledDataSourceDaoV2 implements SampledDataSourceDao {

    private final HbaseAgentStatDaoOperationsV2 operations;

    private final DataSourceDecoder dataSourceDecoder;
    private final DataSourceSampler dataSourceSampler;

    public HbaseSampledDataSourceDaoV2(HbaseAgentStatDaoOperationsV2 operations, DataSourceDecoder dataSourceDecoder, DataSourceSampler dataSourceSampler) {
        this.operations = Objects.requireNonNull(operations, "operations");
        this.dataSourceDecoder = Objects.requireNonNull(dataSourceDecoder, "dataSourceDecoder");
        this.dataSourceSampler = Objects.requireNonNull(dataSourceSampler, "dataSourceSampler");
    }

    @Override
    public List<SampledDataSourceList> getSampledAgentStatList(String agentId, TimeWindow timeWindow) {
        long scanFrom = timeWindow.getWindowRange().getFrom();
        long scanTo = timeWindow.getWindowRange().getTo() + timeWindow.getWindowSlotSize();
        Range range = new Range(scanFrom, scanTo);
        AgentStatMapperV2<DataSourceListBo> mapper = operations.createRowMapper(dataSourceDecoder, range);

        SampledDataSourceResultExtractor resultExtractor = new SampledDataSourceResultExtractor(timeWindow, mapper, dataSourceSampler);
        return operations.getSampledAgentStatList(AgentStatType.DATASOURCE, resultExtractor, agentId, range);
    }

}

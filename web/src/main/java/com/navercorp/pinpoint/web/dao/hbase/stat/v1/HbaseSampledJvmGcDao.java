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

package com.navercorp.pinpoint.web.dao.hbase.stat.v1;

import com.navercorp.pinpoint.common.server.bo.stat.JvmGcBo;
import com.navercorp.pinpoint.web.dao.stat.SampledJvmGcDao;
import com.navercorp.pinpoint.web.mapper.stat.AgentStatMapperV1;
import com.navercorp.pinpoint.web.mapper.stat.sampling.sampler.JvmGcSampler;
import com.navercorp.pinpoint.web.mapper.stat.SampledAgentStatResultExtractor;
import com.navercorp.pinpoint.web.util.TimeWindow;
import com.navercorp.pinpoint.web.vo.Range;
import com.navercorp.pinpoint.web.vo.stat.SampledJvmGc;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @author HyunGil Jeong
 */
@Deprecated
@Repository("sampledJvmGcDaoV1")
public class HbaseSampledJvmGcDao implements SampledJvmGcDao {

    @Autowired
    private AgentStatMapperV1.JvmGcMapper mapper;

    @Autowired
    private JvmGcSampler sampler;

    @Autowired
    private HbaseAgentStatDaoOperations operations;

    @Override
    public List<SampledJvmGc> getSampledAgentStatList(String agentId, TimeWindow timeWindow) {
        long scanFrom = timeWindow.getWindowRange().getFrom();
        long scanTo = timeWindow.getWindowRange().getTo() + timeWindow.getWindowSlotSize();
        Range range = new Range(scanFrom, scanTo);
        SampledAgentStatResultExtractor<JvmGcBo, SampledJvmGc> resultExtractor = new SampledAgentStatResultExtractor<>(timeWindow, mapper, sampler);
        return operations.getSampledAgentStatList(resultExtractor, agentId, range);
    }
}

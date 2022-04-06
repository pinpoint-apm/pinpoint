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

import com.navercorp.pinpoint.common.server.bo.codec.stat.AgentStatDecoder;
import com.navercorp.pinpoint.common.server.bo.stat.AgentStatType;
import com.navercorp.pinpoint.common.server.bo.stat.LoadedClassBo;
import com.navercorp.pinpoint.web.dao.stat.SampledLoadedClassCountDao;
import com.navercorp.pinpoint.web.mapper.stat.sampling.sampler.AgentStatSampler;
import com.navercorp.pinpoint.web.vo.stat.SampledLoadedClassCount;
import org.springframework.stereotype.Repository;

@Repository("sampledLoadedClassDaoV2")
public class HbaseSampledLoadedClassDaoV2
        extends DefaultSampledAgentStatDao<LoadedClassBo, SampledLoadedClassCount> implements SampledLoadedClassCountDao {

    public HbaseSampledLoadedClassDaoV2(HbaseAgentStatDaoOperationsV2 operations,
                                        AgentStatDecoder<LoadedClassBo> decoder,
                                        AgentStatSampler<LoadedClassBo, SampledLoadedClassCount> sampler) {
        super(AgentStatType.LOADED_CLASS, operations, decoder, new SampledAgentStatResultExtractorSupplier<>(sampler));
    }
}

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
import com.navercorp.pinpoint.common.server.bo.stat.FileDescriptorBo;
import com.navercorp.pinpoint.web.dao.stat.SampledFileDescriptorDao;
import com.navercorp.pinpoint.web.mapper.stat.sampling.sampler.AgentStatSampler;
import com.navercorp.pinpoint.web.vo.stat.SampledFileDescriptor;
import org.springframework.stereotype.Repository;

/**
 * @author Roy Kim
 */
@Repository("sampledFileDescriptorDaoV2")
public class HbaseSampledFileDescriptorDaoV2
        extends DefaultSampledAgentStatDao<FileDescriptorBo, SampledFileDescriptor> implements SampledFileDescriptorDao {

    public HbaseSampledFileDescriptorDaoV2(HbaseAgentStatDaoOperationsV2 operations,
                                           AgentStatDecoder<FileDescriptorBo> decoder,
                                           AgentStatSampler<FileDescriptorBo, SampledFileDescriptor> sampler) {
        super(AgentStatType.FILE_DESCRIPTOR, operations, decoder, new SampledAgentStatResultExtractorSupplier<>(sampler));
    }

}

/*
 * Copyright 2019 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.navercorp.pinpoint.collector.mapper.grpc.stat;

import com.navercorp.pinpoint.common.server.bo.stat.JvmGcDetailedBo;
import com.navercorp.pinpoint.grpc.trace.PJvmGcDetailed;
import org.springframework.stereotype.Component;

/**
 * @author HyunGil Jeong
 */
@Component
public class GrpcJvmGcDetailedBoMapper {

    public JvmGcDetailedBo map(final PJvmGcDetailed jvmGcDetailed) {
        final JvmGcDetailedBo jvmGcDetailedBo = new JvmGcDetailedBo();
        jvmGcDetailedBo.setGcNewCount(jvmGcDetailed.getJvmGcNewCount());
        jvmGcDetailedBo.setGcNewTime(jvmGcDetailed.getJvmGcNewTime());
        jvmGcDetailedBo.setCodeCacheUsed(jvmGcDetailed.getJvmPoolCodeCacheUsed());
        jvmGcDetailedBo.setNewGenUsed(jvmGcDetailed.getJvmPoolNewGenUsed());
        jvmGcDetailedBo.setOldGenUsed(jvmGcDetailed.getJvmPoolOldGenUsed());
        jvmGcDetailedBo.setSurvivorSpaceUsed(jvmGcDetailed.getJvmPoolSurvivorSpaceUsed());
        jvmGcDetailedBo.setPermGenUsed(jvmGcDetailed.getJvmPoolPermGenUsed());
        jvmGcDetailedBo.setMetaspaceUsed(jvmGcDetailed.getJvmPoolMetaspaceUsed());
        return jvmGcDetailedBo;
    }
}
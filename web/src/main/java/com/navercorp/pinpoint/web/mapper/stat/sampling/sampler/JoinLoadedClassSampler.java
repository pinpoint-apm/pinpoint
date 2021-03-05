/*
 * Copyright 2020 NAVER Corp.
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

package com.navercorp.pinpoint.web.mapper.stat.sampling.sampler;

import com.navercorp.pinpoint.common.server.bo.stat.join.JoinLoadedClassBo;
import com.navercorp.pinpoint.common.server.bo.stat.join.JoinLongFieldBo;
import com.navercorp.pinpoint.web.vo.stat.AggreJoinLoadedClassBo;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class JoinLoadedClassSampler implements ApplicationStatSampler<JoinLoadedClassBo> {

    @Override
    public AggreJoinLoadedClassBo sampleDataPoints(int timeWindowIndex, long timestamp, List<JoinLoadedClassBo> joinLoadedClassBoList, JoinLoadedClassBo previousDataPoint) {
        if (CollectionUtils.isEmpty(joinLoadedClassBoList)) {
            return AggreJoinLoadedClassBo.createUncollectedObject(timestamp);
        }

        JoinLoadedClassBo joinLoadedClassBo = AggreJoinLoadedClassBo.joinLoadedClassBoList(joinLoadedClassBoList, timestamp);

        String id = joinLoadedClassBo.getId();
        JoinLongFieldBo loadedClassJoinValue = joinLoadedClassBo.getLoadedClassJoinValue();
        JoinLongFieldBo unloadedClassJoinValue = joinLoadedClassBo.getUnloadedClassJoinValue();
        AggreJoinLoadedClassBo aggreJoinLoadedClassBo = new AggreJoinLoadedClassBo(id, loadedClassJoinValue, unloadedClassJoinValue, timestamp);

        return aggreJoinLoadedClassBo;
    }
}

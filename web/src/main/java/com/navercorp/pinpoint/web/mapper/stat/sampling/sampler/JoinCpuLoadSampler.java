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
package com.navercorp.pinpoint.web.mapper.stat.sampling.sampler;

import com.navercorp.pinpoint.common.server.bo.stat.join.JoinCpuLoadBo;
import com.navercorp.pinpoint.common.server.bo.stat.join.JoinDoubleFieldBo;
import com.navercorp.pinpoint.web.vo.stat.AggreJoinCpuLoadBo;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

/**
 * @author minwoo.jung
 */
@Component
public class JoinCpuLoadSampler implements ApplicationStatSampler<JoinCpuLoadBo> {

    @Override
    public AggreJoinCpuLoadBo sampleDataPoints(int timeWindowIndex, long timestamp, List<JoinCpuLoadBo> joinCpuLoadBoList, JoinCpuLoadBo previousDataPoint) {
        if (CollectionUtils.isEmpty(joinCpuLoadBoList)) {
            return AggreJoinCpuLoadBo.createUncollectedObject(timestamp);
        }

        JoinCpuLoadBo joinCpuLoadBo = JoinCpuLoadBo.joinCpuLoadBoList(joinCpuLoadBoList, timestamp);

        String id = joinCpuLoadBo.getId();

        final JoinDoubleFieldBo jvmCpuLoadJoinValue = joinCpuLoadBo.getJvmCpuLoadJoinValue();
        double jvmCpuLoad = roundToScale(jvmCpuLoadJoinValue.getAvg() * 100);
        double minJvmCpuLoad = roundToScale(jvmCpuLoadJoinValue.getMin() * 100);
        String minJvmCpuAgentId = jvmCpuLoadJoinValue.getMinAgentId();
        double maxJvmCpuLoad = roundToScale(jvmCpuLoadJoinValue.getMax() * 100);
        String maxJvmCpuAgentId = jvmCpuLoadJoinValue.getMaxAgentId();

        final JoinDoubleFieldBo systemCpuLoadJoinValue = joinCpuLoadBo.getSystemCpuLoadJoinValue();
        double sysCpuLoad = roundToScale(systemCpuLoadJoinValue.getAvg() * 100);
        double minSysCpuLoad = roundToScale(systemCpuLoadJoinValue.getMin() * 100);
        String minSysCpuAgentId = systemCpuLoadJoinValue.getMinAgentId();
        double maxSysCpuLoad = roundToScale(systemCpuLoadJoinValue.getMax() * 100);
        String maxSysCpuAgentId = systemCpuLoadJoinValue.getMaxAgentId();

        AggreJoinCpuLoadBo aggreJoinCpuLoadBo = new AggreJoinCpuLoadBo(id, jvmCpuLoad, maxJvmCpuLoad, maxJvmCpuAgentId, minJvmCpuLoad, minJvmCpuAgentId, sysCpuLoad, maxSysCpuLoad, maxSysCpuAgentId, minSysCpuLoad, minSysCpuAgentId, timestamp);
        return aggreJoinCpuLoadBo;
    }

    private double roundToScale(double value) {
        return BigDecimal.valueOf(value).setScale(1, RoundingMode.HALF_UP).doubleValue();
    }

}

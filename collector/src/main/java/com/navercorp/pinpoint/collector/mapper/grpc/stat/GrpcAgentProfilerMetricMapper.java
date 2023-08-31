/*
 * Copyright 2023 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.navercorp.pinpoint.collector.mapper.grpc.stat;

import com.navercorp.pinpoint.common.server.bo.stat.ProfilerMetricBo;
import com.navercorp.pinpoint.grpc.Header;
import com.navercorp.pinpoint.grpc.server.ServerContext;
import com.navercorp.pinpoint.grpc.trace.PProfilerMetric;
import com.navercorp.pinpoint.grpc.trace.PProfilerMetricField;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class GrpcAgentProfilerMetricMapper {

    public ProfilerMetricBo map(PProfilerMetric profilerMetric) {
        if (profilerMetric == null) {
            return null;
        }

        final Header agentInfo = ServerContext.getAgentInfo();
        final String agentId = agentInfo.getAgentId();
        final long startTimestamp = agentInfo.getAgentStartTime();

        final ProfilerMetricBo profilerMetricBo = new ProfilerMetricBo();
        profilerMetricBo.setAgentId(agentId);
        profilerMetricBo.setStartTimestamp(startTimestamp);
        profilerMetricBo.setTimestamp(profilerMetric.getTimestamp());
        profilerMetricBo.setMetricName(profilerMetric.getName());

        List<PProfilerMetricField> tags = profilerMetric.getTagsList();
        for (PProfilerMetricField tag : tags) {
            profilerMetricBo.addTags(tag.getName(), tag.getStringValue());
        }

        List<PProfilerMetricField> fields = profilerMetric.getFieldsList();
        for (PProfilerMetricField field : fields) {
            profilerMetricBo.addValues(field.getName(), field.getLongValue());
        }

        return profilerMetricBo;
    }
}

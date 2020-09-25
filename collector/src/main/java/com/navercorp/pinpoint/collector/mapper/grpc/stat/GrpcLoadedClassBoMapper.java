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

package com.navercorp.pinpoint.collector.mapper.grpc.stat;

import com.navercorp.pinpoint.common.server.bo.stat.AgentStatBo;
import com.navercorp.pinpoint.common.server.bo.stat.LoadedClassBo;
import com.navercorp.pinpoint.grpc.trace.PAgentStat;
import com.navercorp.pinpoint.grpc.trace.PLoadedClass;
import org.springframework.stereotype.Component;

@Component
public class GrpcLoadedClassBoMapper implements GrpcStatMapper {
    public LoadedClassBo map(final PLoadedClass loadedClass) {
        final LoadedClassBo loadedClassBo = new LoadedClassBo();
        loadedClassBo.setLoadedClassCount(loadedClass.getLoadedClassCount());
        loadedClassBo.setUnloadedClassCount(loadedClass.getUnloadedClassCount());
        return loadedClassBo;
    }

    @Override
    public void map(AgentStatBo.Builder.StatBuilder builder, PAgentStat agentStat) {
        // loadedClass
        if (agentStat.hasLoadedClass()) {
            final PLoadedClass loadedClass = agentStat.getLoadedClass();
            final LoadedClassBo loadedClassBo = this.map(loadedClass);
            builder.addLoadedClass(loadedClassBo);
        }
    }
}

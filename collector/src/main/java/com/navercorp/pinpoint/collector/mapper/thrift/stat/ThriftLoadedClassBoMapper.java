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

package com.navercorp.pinpoint.collector.mapper.thrift.stat;

import com.navercorp.pinpoint.common.server.bo.stat.AgentStatBo;
import com.navercorp.pinpoint.common.server.bo.stat.LoadedClassBo;
import com.navercorp.pinpoint.thrift.dto.TAgentStat;
import com.navercorp.pinpoint.thrift.dto.TLoadedClass;
import org.springframework.stereotype.Component;

@Component
public class ThriftLoadedClassBoMapper implements ThriftStatMapper<LoadedClassBo, TLoadedClass> {
    @Override
    public LoadedClassBo map(TLoadedClass thriftObject) {
        LoadedClassBo loadedClassBo = new LoadedClassBo();
        loadedClassBo.setLoadedClassCount(thriftObject.getLoadedClassCount());
        loadedClassBo.setUnloadedClassCount(thriftObject.getUnloadedClassCount());
        return loadedClassBo;
    }

    @Override
    public void map(AgentStatBo.Builder.StatBuilder agentStatBo, TAgentStat tAgentStat) {
        // not supported
    }
}

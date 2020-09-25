/*
 * Copyright 2020 Naver Corp.
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
import com.navercorp.pinpoint.common.server.bo.stat.TotalThreadCountBo;
import com.navercorp.pinpoint.thrift.dto.TAgentStat;
import com.navercorp.pinpoint.thrift.dto.TTotalThreadCount;
import org.springframework.stereotype.Component;

@Component
public class ThriftTotalThreadCountBoMapper implements ThriftStatMapper<TotalThreadCountBo, TTotalThreadCount> {
    @Override
    public TotalThreadCountBo map(TTotalThreadCount thriftObject) {
        TotalThreadCountBo totalThreadCountBo = new TotalThreadCountBo();
        totalThreadCountBo.setTotalThreadCount(thriftObject.getTotalThreadCount());
        return totalThreadCountBo;
    }

    @Override
    public void map(AgentStatBo.Builder.StatBuilder agentStatBo, TAgentStat tAgentStat) {
        // TODO: not supported
//        if (tAgentStat.isSetTotalThreadCount()) {
//            TotalThreadCountBo totalThreadCountBo = this.map(tAgentStat.getTotalThreadCount());
//        }
    }
}

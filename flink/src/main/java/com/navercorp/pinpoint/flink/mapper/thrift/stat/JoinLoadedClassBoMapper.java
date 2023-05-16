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

package com.navercorp.pinpoint.flink.mapper.thrift.stat;

import com.navercorp.pinpoint.common.server.bo.stat.join.JoinAgentStatBo;
import com.navercorp.pinpoint.common.server.bo.stat.join.JoinLoadedClassBo;
import com.navercorp.pinpoint.common.server.bo.stat.join.JoinLongFieldBo;
import com.navercorp.pinpoint.thrift.dto.flink.TFAgentStat;

public class JoinLoadedClassBoMapper implements ThriftStatMapper<JoinLoadedClassBo, TFAgentStat> {
    @Override
    public JoinLoadedClassBo map(TFAgentStat thriftObject) {
        if(!thriftObject.isSetLoadedClass()) {
            return JoinLoadedClassBo.EMPTY_JOIN_LOADED_CLASS_BO;
        }
        JoinLoadedClassBo joinLoadedClassBo = new JoinLoadedClassBo();

        final String agentId = thriftObject.getAgentId();
        final long loadedClass = thriftObject.getLoadedClass().getLoadedClassCount();
        final long unloadedClass = thriftObject.getLoadedClass().getUnloadedClassCount();

        joinLoadedClassBo.setId(agentId);
        joinLoadedClassBo.setTimestamp(thriftObject.getTimestamp());
        joinLoadedClassBo.setLoadedClassJoinValue(new JoinLongFieldBo(loadedClass, loadedClass, agentId, loadedClass, agentId));
        joinLoadedClassBo.setUnloadedClassJoinValue(new JoinLongFieldBo(unloadedClass, unloadedClass, agentId, unloadedClass, agentId));

        return joinLoadedClassBo;
    }


    @Override
    public void build(TFAgentStat tFAgentStat, JoinAgentStatBo.Builder builder) {
        JoinLoadedClassBo joinLoadedClassBo = this.map(tFAgentStat);

        if (joinLoadedClassBo == JoinLoadedClassBo.EMPTY_JOIN_LOADED_CLASS_BO) {
            return;
        }

        builder.addLoadedClass(joinLoadedClassBo);
    }
}

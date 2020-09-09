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

package com.navercorp.pinpoint.web.vo.stat;

import com.navercorp.pinpoint.common.server.bo.stat.join.JoinContainerBo;
import com.navercorp.pinpoint.common.server.bo.stat.join.JoinDoubleFieldBo;
import com.navercorp.pinpoint.common.server.bo.stat.join.JoinLongFieldBo;

/**
 * @author Hyunjoon Cho
 */
public class AggreJoinContainerBo extends JoinContainerBo implements AggregationStatData {

    public AggreJoinContainerBo(){
    }

    public AggreJoinContainerBo(String id, double avgUserCpuUsage, double maxUserCpuUsage, String maxUserCpuUsageAgentId, double minUserCpuUsage, String minUserCpuUsageAgentId
            , double avgSystemCpuUsage, double maxSystemCpuUsage, String maxSystemCpuUsageAgentId, double minSystemCpuUsage, String minSystemCpuUsageAgentId
            , long avgMemoryMax, long maxMemoryMax, String maxMemoryMaxAgentId, long minMemoryMax, String minMemoryMaxAgentId
            , long avgMemoryUsage, long maxMemoryUsage, String maxMemoryUsageAgentId, long minMemoryUsage, String minMemoryUsageAgentId
            , long timestamp){
        super(id,
                avgUserCpuUsage, minUserCpuUsage, minUserCpuUsageAgentId, maxUserCpuUsage, maxUserCpuUsageAgentId,
                avgSystemCpuUsage, minSystemCpuUsage, minSystemCpuUsageAgentId, maxSystemCpuUsage, maxSystemCpuUsageAgentId,
                avgMemoryMax, minMemoryMax, minMemoryMaxAgentId, maxMemoryMax, maxMemoryMaxAgentId,
                avgMemoryUsage, minMemoryUsage, minMemoryUsageAgentId, maxMemoryUsage, maxMemoryUsageAgentId, timestamp);
    }

    public AggreJoinContainerBo(String id, JoinDoubleFieldBo userCpuUsageJoinValue, JoinDoubleFieldBo systemCpuUsageJoinValue,
                                JoinLongFieldBo memoryMaxJoinValue, JoinLongFieldBo memoryUsageJoinValue, long timestamp) {
        super(id, userCpuUsageJoinValue, systemCpuUsageJoinValue, memoryMaxJoinValue, memoryUsageJoinValue, timestamp);
    }

    public static AggreJoinContainerBo createUncollectedObject(long timestamp) {
        AggreJoinContainerBo aggreJoinContainerBo = new AggreJoinContainerBo();
        aggreJoinContainerBo.setTimestamp(timestamp);
        return aggreJoinContainerBo;
    }

}

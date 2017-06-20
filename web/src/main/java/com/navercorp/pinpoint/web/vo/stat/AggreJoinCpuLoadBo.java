/*
 * Copyright 2017 NAVER Corp.
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
package com.navercorp.pinpoint.web.vo.stat;

import com.navercorp.pinpoint.common.server.bo.stat.join.JoinCpuLoadBo;

/**
 * @author minwoo.jung
 */
public class AggreJoinCpuLoadBo extends JoinCpuLoadBo implements AggregationStatData {

    public AggreJoinCpuLoadBo() {
    }

    public AggreJoinCpuLoadBo(String id, double jvmCpuLoad, double maxJvmCpuLoad, String maxJvmCpuAgentId, double minJvmCpuLoad, String minJvmCpuAgentId, double systemCpuLoad, double maxSystemCpuLoad, String maxSysCpuAgentId, double minSystemCpuLoad, String minSysCpuAgentId, long timestamp) {
        super(id, jvmCpuLoad, maxJvmCpuLoad, maxJvmCpuAgentId, minJvmCpuLoad, minJvmCpuAgentId, systemCpuLoad, maxSystemCpuLoad, maxSysCpuAgentId, minSystemCpuLoad, minSysCpuAgentId, timestamp);
    }

    public static AggreJoinCpuLoadBo createUncollectedObject(long timestamp) {
        AggreJoinCpuLoadBo aggreJoinCpuLoadBo = new AggreJoinCpuLoadBo();
        aggreJoinCpuLoadBo.setTimestamp(timestamp);
        return aggreJoinCpuLoadBo;
    }
}

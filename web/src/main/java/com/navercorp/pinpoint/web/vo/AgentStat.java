/*
 * Copyright 2014 NAVER Corp.
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

package com.navercorp.pinpoint.web.vo;

import com.navercorp.pinpoint.common.bo.AgentStatCpuLoadBo;
import com.navercorp.pinpoint.common.bo.AgentStatMemoryGcBo;

/**
 * @author hyungil.jeong
 */
public class AgentStat {

    private AgentStatMemoryGcBo memoryGc;
    private AgentStatCpuLoadBo cpuLoad;

    public AgentStatMemoryGcBo getMemoryGc() {
        return memoryGc;
    }

    public void setMemoryGc(AgentStatMemoryGcBo memoryGc) {
        this.memoryGc = memoryGc;
    }

    public AgentStatCpuLoadBo getCpuLoad() {
        return cpuLoad;
    }

    public void setCpuLoad(AgentStatCpuLoadBo cpuLoad) {
        this.cpuLoad = cpuLoad;
    }
}

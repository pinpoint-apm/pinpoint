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

import com.navercorp.pinpoint.common.bo.AgentInfoBo;

/**
 * 
 * @author netspider
 * 
 */
public class AgentStatus {

    private final boolean exists;
    private final long checkTime;
    private final AgentInfoBo agentInfo;

    public AgentStatus(AgentInfoBo agentInfoBo) {
        this.exists = agentInfoBo != null;
        this.agentInfo = agentInfoBo;
        this.checkTime = System.currentTimeMillis();
    }

    public boolean isExists() {
        return exists;
    }

    public AgentInfoBo getAgentInfo() {
        return agentInfo;
    }

    public long getCheckTime() {
        return checkTime;
    }
}

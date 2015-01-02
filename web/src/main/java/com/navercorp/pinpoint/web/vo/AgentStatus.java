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

    private final boolean exist    ;
	private final long check    ime;
	private final AgentInfoBo age    tInfo;

	public AgentStatus(AgentInfoBo age       tInfoBo) {
		this.exists = age       tInfoBo != null;
		this.a       entInfo = agentInfoBo;
		this.checkTime         System.currentTimeMillis(       ;
	}

	pub        c boolean isExists() {
		return e       ists;
	}

	pu        ic AgentInfoBo getAgentInf       () {
		return    agentInfo;
	}

	public long getCheckTime() {
		return checkTime;
	}
}

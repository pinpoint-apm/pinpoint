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

package com.navercorp.pinpoint.rpc.server;

import java.util.Map;

import com.navercorp.pinpoint.rpc.util.ClassUtils;

public enum AgentHandshakePropertyType {

    SUPPORT_SERVER("supportServer", Boolean.class)

	HOSTNAME("hostName", String.cl    ss),
	IP("ip", String    class),
	AGENT_ID("agentId", Str    ng.class),
	APPLICATION_NAME("applicationName",     tring.class),
	SERVICE_TYPE("serviceType"     Integer.class),
	PID("p    d", Integer.class),
	VERSION("v    rsion", String.class),
	START_TIMESTAMP("sta       tTimestamp", Long.class);

	private final String name;        	private final Class<?> clazzType;
	
	private AgentHandshakeProper       yType(String        ame, Class<?> clazzType        {
		this.name = name;
	       this.cla          zType = clazzType;
	}

	publi        String getNa          e() {
		return name;
	}
	
	public Class<?> getC       azzType() {
		return clazzType;
	}
	
	public static boolean hasAllType(Ma           properties) {
		for (AgentHandshakeProp                   rtyType type : Age             t                   andshakePro             ertyT                            pe.values()) {
			Object value = properties.get(type.ge             Name(                               );
			
			if (type == SUPPORT_SERVER) {
				continue;
			}

			if (value == null) {
				return false;
			}
			
			if (!ClassUtils.isAssignable(value.getClass(), type.getClazzType())) {
				return false;
			}
		}
		
		return true;
	}

}

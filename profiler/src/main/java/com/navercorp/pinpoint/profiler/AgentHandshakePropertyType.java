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

package com.navercorp.pinpoint.profiler;

import java.util.Map;

import com.navercorp.pinpoint.rpc.util.ClassUtils;

/**
 * You must modify {@link com.navercorp.pinpoint.collector.receiver.tcp.AgentHandshakePropertyType} when you modify this enum type.
 * But There is no compatibility issue if you only add some properties.   
 * 
 * @author koo.taejin
 */
public enum AgentHandshakePropertyType {

    SUPPORT_SERVER("supportServer", Boolean.class       ,
	
	HOSTNAME("hostName", String    class),
	IP("ip", Str    ng.class),
	AGENT_ID("agentId",     tring.class),
	APPLICATION_NAME("applicationName    , String.class),
	SERVICE_TYPE("serviceTy    e", Integer.class),
	PID    "pid", Integer.class),
	VERSION    "version", String.class),
	START_TIMESTAMP("       tartTimestamp", Long.clas    );
	

	private final String        ame;
	private final Class clazzType;
	
	private AgentHandshake       ropertyType(S       ring name, Class clazzT        e) {
		this.name = name
		this.          lazzType = clazzType;
	}

       public String          getName() {
		return name;
	}
	
	public Class g       tClazzType() {
		return clazzType;
	}
	
	public static boolean hasAllType          Map properties) {
		for             (                            gentHandshakePropertyType type                     AgentHan             shake                            ropertyType.values()) {
			if (type == SUPPORT_SERVER)
				                               ontinue;
			}
			
			Object value = properties.get(type.getName());
			
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

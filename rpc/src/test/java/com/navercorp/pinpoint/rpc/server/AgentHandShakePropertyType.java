package com.nhn.pinpoint.rpc.server;

import java.util.Map;

import com.nhn.pinpoint.rpc.util.ClassUtils;

public enum AgentHandShakePropertyType {

	SUPPORT_SERVER("supportServer", Boolean.class),

	HOSTNAME("hostName", String.class),
	IP("ip", String.class),
	AGENT_ID("agentId", String.class),
	APPLICATION_NAME("applicationName", String.class),
	SERVICE_TYPE("serviceType", Integer.class),
	PID("pid", Integer.class),
	VERSION("version", String.class),
	START_TIMESTAMP("startTimestamp", Long.class);
	

	private final String name; 
	private final Class<?> clazzType;
	
	private AgentHandShakePropertyType(String name, Class<?> clazzType) {
		this.name = name;
		this.clazzType = clazzType;
	}

	public String getName() {
		return name;
	}
	
	public Class<?> getClazzType() {
		return clazzType;
	}
	
	public static boolean hasAllType(Map properties) {
		for (AgentHandShakePropertyType type : AgentHandShakePropertyType.values()) {
			Object value = properties.get(type.getName());
			
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

package com.nhn.pinpoint.rpc.server;

import java.util.Map;

import com.nhn.pinpoint.rpc.util.ClassUtils;

public enum AgentPropertiesType {

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
	
	private AgentPropertiesType(String name, Class<?> clazzType) {
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
		for (AgentPropertiesType type : AgentPropertiesType.values()) {
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

package com.nhn.pinpoint.profiler;

import java.util.Map;

import com.nhn.pinpoint.rpc.util.ClassUtils;

/**
 * @author koo.taejin
 */
public enum AgentPropertiesType {

	// 해당 객체는 profiler, collector 양쪽에 함꼐 있음 
	// 변경시 함께 변경 필요
	// map으로 처리하기 때문에 이전 파라미터 제거 대신 추가할 경우 확장성에는 문제가 없음

	HOSTNAME("hostName", String.class),
	IP("ip", String.class),
	AGENT_ID("agentId", String.class),
	APPLICATION_NAME("applicationName", String.class),
	SERVICE_TYPE("serviceType", Integer.class),
	PID("pid", Integer.class),
	VERSION("version", String.class),
	START_TIMESTAMP("startTimestamp", Long.class);
	

	private final String name; 
	private final Class clazzType;
	
	private AgentPropertiesType(String name, Class clazzType) {
		this.name = name;
		this.clazzType = clazzType;
	}

	public String getName() {
		return name;
	}
	
	public Class getClazzType() {
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

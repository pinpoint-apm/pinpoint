package com.nhn.pinpoint.collector.receiver.tcp;

import java.util.Map;

import com.nhn.pinpoint.rpc.util.ClassUtils;

public enum AgentHandShakePropertyType {

	// 해당 객체는 profiler, collector 양쪽에 함꼐 있음 
	// 변경시 함께 변경 필요
	// map으로 처리하기 때문에 이전 파라미터 제거 대신 추가할 경우 확장성에는 문제가 없음

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
	private final Class clazzType;
	
	private AgentHandShakePropertyType(String name, Class clazzType) {
		this.name = name;
		this.clazzType = clazzType;
	}

	public String getName() {
		return name;
	}
	
	public Class getClazzType() {
		return clazzType;
	}
	
	public static boolean hasAllType(Map<Object, Object> properties) {
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

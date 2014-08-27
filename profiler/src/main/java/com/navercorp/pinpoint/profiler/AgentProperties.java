package com.nhn.pinpoint.profiler;

import java.util.Map;

import com.nhn.pinpoint.rpc.util.ClassUtils;

/**
 * @author koo.taejin
 */
public class AgentProperties {

	// 해당 객체는 profiler, collector 양쪽에 함꼐 있음 
	// 변경시 함께 변경 필요
	// map으로 처리하기 때문에 이전 파라미터 제거 대신 추가할 경우 확장성에는 문제가 없음
	
	public static final String KEY_HOSTNAME = "hostName";
	public static final String KEY_IP = "ip";
	public static final String KEY_AGENTID = "agentId";
	public static final String KEY_APPLICATION_NAME = "applicationName";
	public static final String KEY_SERVICE_TYPE = "serviceType";
	public static final String KEY_PID = "pid";
	public static final String KEY_VERSION = "version";
	public static final String KEY_START_TIME_MILLIS = "startTimestamp";
	
	private final Map<String, Object> properties;

	public AgentProperties(Map<String, Object> properties) {
		this.properties = properties;
	}

	public <T> T getProperties(String key, Class<T> returnClazz) {
		Object value = properties.get(key);

		if (value == null) {
			return null;
		}

		if (ClassUtils.isAssignable(value.getClass(), returnClazz)) {
			return (T) value;
		}

		return null;
	}

}

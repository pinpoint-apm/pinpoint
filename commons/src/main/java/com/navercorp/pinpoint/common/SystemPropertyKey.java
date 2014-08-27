package com.nhn.pinpoint.common;

/**
 * @author hyungil.jeong
 */
public enum SystemPropertyKey {
	
	JAVA_VERSION("java.version"),
	JAVA_RUNTIME_VERSION("java.runtime.version"),
	JAVA_RUNTIME_NAME("java.runtime.name"),
	JAVA_SPECIFICATION_VERSION("java.specification.version"),
	JAVA_CLASS_VERSION("java.class.version"),
	JAVA_VM_NAME("java.vm.name"),
	JAVA_VM_VERSION("java.vm.version"),
	JAVA_VM_INFO("java.vm.info"),
	JAVA_VM_SPECIFICATION_VERSION("java.vm.specification.version");
	
	private final String key;
	
	private SystemPropertyKey(String key) {
		this.key = key;
	}
	
	public String getKey() {
		return this.key;
	}
}

package com.profiler.common;

public enum ServiceType {
	
	TOMCAT("TOMCAT"),
	BLOC("BLOC"),
	MEMCACHED("MEMCACHED"),
	ARCUS("ARCUS"),
	MYSQL("MYSQL"),
	UNKNOWN("UNKNOWN");

	public static final String DELIMETER = "/";
	
	private String code;

	ServiceType(String code) {
		this.code = code;
	}
	
	public static ServiceType parseServerType(String applicationName) {
		if (applicationName == null)
			return ServiceType.UNKNOWN;

		if (applicationName.startsWith(TOMCAT.code)) {
			return TOMCAT;
		} else if (applicationName.startsWith(BLOC.code)) {
			return BLOC;
		} else if (applicationName.startsWith(MEMCACHED.code)) {
			return MEMCACHED;
		} else if (applicationName.startsWith(ARCUS.code)) {
			return ARCUS;
		} else if (applicationName.startsWith(MYSQL.code)) {
			return MYSQL;
		}
		
		return ServiceType.UNKNOWN;
	}
	
	@Override
	public String toString() {
		return code;
	}
}

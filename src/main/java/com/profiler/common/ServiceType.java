package com.profiler.common;

public enum ServiceType {

	TOMCAT((short) 0, "TOMCAT", false), 
	BLOC((short) 1, "BLOC", true), 
	MEMCACHED((short) 2, "MEMCACHED", true), 
	ARCUS((short) 3, "ARCUS", true),
	MYSQL((short) 4, "MYSQL", true), 
	UNKNOWN((short) -1, "UNKNOWN", false);

	private short code;
	private String desc;
	private boolean terminal;

	ServiceType(short code, String desc, boolean terminal) {
		this.code = code;
		this.desc = desc;
		this.terminal = terminal;
	}

	public static ServiceType parse(short code) {
		if (TOMCAT.code == code) {
			return TOMCAT;
		} else if (BLOC.code == code) {
			return BLOC;
		} else if (MEMCACHED.code == code) {
			return MEMCACHED;
		} else if (ARCUS.code == code) {
			return ARCUS;
		} else if (MYSQL.code == code) {
			return MYSQL;
		} else {
			return UNKNOWN;
		}
	}

	public short getCode() {
		return code;
	}
	
	@Override
	public String toString() {
		return desc;
	}
}

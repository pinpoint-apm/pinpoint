package com.profiler.common;

public enum ServiceType {
	
	UNKNOWN(		(short) 0,		"UNKNOWN",		false),
	UNKNOWN_CLOUD(	(short) 1,		"UNKNOWN_EXT",	false),
	
	TOMCAT(			(short) 1001,	"TOMCAT",		false), 
	BLOC(			(short) 1002,	"BLOC",			true),
	
	MEMCACHED(		(short) 2001,	"MEMCACHED",	true), 
	ARCUS(			(short) 2002,	"ARCUS",		true),
	
	MYSQL(			(short) 3001,	"MYSQL",		true),
	MSSQL(			(short) 3002,	"MSSQL",		true),
	ORACLE(			(short) 3003,	"ORACLE",		true),
	CUBRID(			(short) 3004,	"CUBRID",		true),
	
	HTTP_CLIENT(	(short) 9001,	"HTTP_CLIENT",	false);
	
	private short code;
	private String desc;
	private boolean terminal;

	ServiceType(short code, String desc, boolean terminal) {
		this.code = code;
		this.desc = desc;
		this.terminal = terminal;
	}

	public static ServiceType parse(short code) {
		ServiceType[] values = ServiceType.values();
		for (ServiceType type : values) {
			if (type.code == code) {
				return type;
			}
		}
		return UNKNOWN;
	}
	
	public static ServiceType parse(String desc) {
		ServiceType[] values = ServiceType.values();
		for (ServiceType type : values) {
			if (type.desc.equals(desc)) {
				return type;
			}
		}
		return UNKNOWN;
	}

	public boolean isRpcClient() {
		return code >= 9000 && code < 10000;
	}
	
	public short getCode() {
		return code;
	}
	
	public String getDesc() {
		return desc;
	}

	public boolean isTerminal() {
		return terminal;
	}
	
	@Override
	public String toString() {
		return desc;
	}
}

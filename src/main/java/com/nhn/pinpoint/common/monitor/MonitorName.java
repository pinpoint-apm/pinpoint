package com.nhn.pinpoint.common.monitor;

public class MonitorName {

	public static final String DEFAULT_PREFIX = "common";

	private String prefix;
	private String subname;
	private String name;

	public MonitorName(String subname) {
		this(DEFAULT_PREFIX, subname);
	}

	public MonitorName(String prefix, String subname) {
		this.prefix = prefix == null ? DEFAULT_PREFIX : prefix;
		this.subname = subname == null ? "unknown" : subname;
		this.name = this.prefix + "." + this.subname;
	}

	public String getPrefix() {
		return prefix;
	}

	public String getSubname() {
		return subname;
	}

	public String getName() {
		return name;
	}

	public String toString() {
		return "MonitorName(" + this.name + ")";
	}
	
}

package com.nhn.pinpoint.profiler.monitor;

public class MonitorName {

	private String name;

	public MonitorName(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public String toString() {
		return "MonitorName(" + this.name + ")";
	}
	
}

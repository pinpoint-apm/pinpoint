package com.nhn.hippo.web.vo.callstacks;

/**
 * each stack
 * 
 * @author netspider
 * 
 */
public class Record {
	private final int tab;
	private final boolean method;

	private final String title;
	private final String arguments;
	private final long begin;
	private final long elapsed;
	private final String agent;
	private final String service;

	public Record(int tab, boolean method, String title, String arguments, long begin, long elapsed, String agent, String service) {
		this.tab = tab;
		this.method = method;

		this.title = title;
		this.arguments = arguments;
		this.begin = begin;
		this.elapsed = elapsed;
		this.agent = agent;
		this.service = service;
	}

	public int getTab() {
		return tab;
	}

	public boolean isMethod() {
		return method;
	}

	public String getTitle() {
		return title;
	}

	public String getArguments() {
		return arguments;
	}

	public long getBegin() {
		return begin;
	}

	public long getElapsed() {
		return elapsed;
	}

	public String getAgent() {
		return agent;
	}

	public String getService() {
		return service;
	}

	@Override
	public String toString() {
		return "Record [tab=" + tab + ", method=" + method + ", title=" + title + ", arguments=" + arguments + ", begin=" + begin + ", elapsed=" + elapsed + ", agent=" + agent + ", service=" + service + "]";
	}
}

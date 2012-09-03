package com.profiler.context;

public enum Annotation {
	ClientSend("CS"), ClientRecv("CR"), ServerSend("SS"), ServerRecv("SR");

	private String code;

	Annotation(String code) {
		this.code = code;
	}

	public String getCode() {
		return this.code;
	}
}

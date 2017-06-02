package com.navercorp.pinpoint.bootstrap.config;

public enum TransportType {
	UDP("UDP"), TCP("TCP");

	private String type;

	TransportType(String type) {
		this.type = type;
	}

	public String type() {
		return type;
	}
}

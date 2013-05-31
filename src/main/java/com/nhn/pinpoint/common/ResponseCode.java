package com.nhn.pinpoint.common;

public enum ResponseCode {
	NORMAL(0), WARN(1), SLOW(2), ERROR(3);

	int code;

	ResponseCode(int code) {
		this.code = code;
	}

	public int getCode() {
		return this.code;
	}
}

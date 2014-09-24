package com.nhn.pinpoint.profiler.interceptor.bci;

// TODO 추후 별도 계층구조가 필요하면 분화 필요.
/**
 * @author emeroad
 */
public class InstrumentException extends Exception {

	private static final long serialVersionUID = 7594176009977030312L;

	public InstrumentException() {
	}

	public InstrumentException(String message) {
		super(message);
	}

	public InstrumentException(String message, Throwable cause) {
		super(message, cause);
	}

	public InstrumentException(Throwable cause) {
		super(cause);
	}
}

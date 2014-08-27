package com.nhn.pinpoint.collector.cluster.zookeeper.exception;

/**
 * @author koo.taejin
 */
public class AuthException extends PinpointZookeeperException {

	public AuthException() {
	}

	public AuthException(String message) {
		super(message);
	}

	public AuthException(String message, Throwable cause) {
		super(message, cause);
	}

	public AuthException(Throwable cause) {
		super(cause);
	}

}

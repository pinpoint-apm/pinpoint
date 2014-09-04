package com.nhn.pinpoint.web.cluster.zookeeper.exception;

/**
 * @author koo.taejin <kr14910>
 */
public class TimeoutException extends PinpointZookeeperException {

	public TimeoutException() {
	}

	public TimeoutException(String message) {
		super(message);
	}

	public TimeoutException(String message, Throwable cause) {
		super(message, cause);
	}

	public TimeoutException(Throwable cause) {
		super(cause);
	}

}

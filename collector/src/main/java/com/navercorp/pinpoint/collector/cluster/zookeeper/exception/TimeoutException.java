package com.nhn.pinpoint.collector.cluster.zookeeper.exception;

/**
 * @author koo.taejin
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

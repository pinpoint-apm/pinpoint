package com.nhn.pinpoint.collector.cluster.zookeeper.exception;

/**
 * @author koo.taejin
 */
public class BadOperationException extends PinpointZookeeperException {

	public BadOperationException() {
	}

	public BadOperationException(String message) {
		super(message);
	}

	public BadOperationException(String message, Throwable cause) {
		super(message, cause);
	}

	public BadOperationException(Throwable cause) {
		super(cause);
	}

}

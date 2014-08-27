package com.nhn.pinpoint.collector.cluster.zookeeper.exception;

/**
 * @author koo.taejin
 */
public class UnknownException extends PinpointZookeeperException {

	public UnknownException() {
	}

	public UnknownException(String message) {
		super(message);
	}

	public UnknownException(String message, Throwable cause) {
		super(message, cause);
	}

	public UnknownException(Throwable cause) {
		super(cause);
	}

}

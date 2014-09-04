package com.nhn.pinpoint.web.cluster.zookeeper.exception;

/**
 * @author koo.taejin <kr14910>
 */
public class ConnectionException extends PinpointZookeeperException {

	public ConnectionException() {
	}

	public ConnectionException(String message) {
		super(message);
	}

	public ConnectionException(String message, Throwable cause) {
		super(message, cause);
	}

	public ConnectionException(Throwable cause) {
		super(cause);
	}

}

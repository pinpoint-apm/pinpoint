package com.nhn.pinpoint.web.cluster.zookeeper.exception;

/**
 * @author koo.taejin <kr14910>
 */
public class PinpointZookeeperException extends Exception {
	
	public PinpointZookeeperException() {
	}

	public PinpointZookeeperException(String message) {
		super(message);
	}

	public PinpointZookeeperException(String message, Throwable cause) {
		super(message, cause);
	}

	public PinpointZookeeperException(Throwable cause) {
		super(cause);
	}

}

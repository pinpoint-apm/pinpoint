package com.nhn.pinpoint.web.cluster.zookeeper.exception;

public class NoNodeException extends PinpointZookeeperException {

    public NoNodeException() {
    }

    public NoNodeException(String message) {
        super(message);
    }

    public NoNodeException(String message, Throwable cause) {
        super(message, cause);
    }

    public NoNodeException(Throwable cause) {
        super(cause);
    }

}
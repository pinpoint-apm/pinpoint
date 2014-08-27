package com.nhn.pinpoint.rpc.control;

/**
 * @author koo.taejin
 */
public class ProtocolException extends Exception {

    public ProtocolException() {
    }

    public ProtocolException(String message) {
        super(message);
    }

    public ProtocolException(Throwable cause) {
        super(cause);
    }

    public ProtocolException(String message, Throwable cause) {
        super(message, cause);
    }
    
}

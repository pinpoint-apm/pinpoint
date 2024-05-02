package com.navercorp.pinpoint.common.hbase;

public class RequestNotPermittedException extends RuntimeException {

    public RequestNotPermittedException(String message, boolean writableStackTrace) {
        super(message, null, false, writableStackTrace);
    }
}

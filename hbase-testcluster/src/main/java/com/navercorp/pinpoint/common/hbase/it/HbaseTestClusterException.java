package com.navercorp.pinpoint.common.hbase.it;

public class HbaseTestClusterException extends RuntimeException {
    public HbaseTestClusterException() {
    }

    public HbaseTestClusterException(String message) {
        super(message);
    }

    public HbaseTestClusterException(String message, Throwable cause) {
        super(message, cause);
    }

    public HbaseTestClusterException(Throwable cause) {
        super(cause);
    }

    public HbaseTestClusterException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

}

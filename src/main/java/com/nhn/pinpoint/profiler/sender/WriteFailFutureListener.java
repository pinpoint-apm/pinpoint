package com.nhn.pinpoint.profiler.sender;

import com.nhn.pinpoint.profiler.logging.Logger;
import com.nhn.pinpoint.rpc.Future;
import com.nhn.pinpoint.rpc.FutureListener;

public class WriteFailFutureListener implements FutureListener {

    private final Logger logger;
    private final String message;
    private final String host;
    private final String port;


    public WriteFailFutureListener(Logger logger, String message, String host, int port) {
        if (logger == null) {
            throw new NullPointerException("logger must not be null");
        }
        this.logger = logger;
        this.message = message;
        this.host = host;
        this.port = String.valueOf(port);
    }

    @Override
    public void onComplete(Future future) {
        if (!future.isSuccess()) {
            logger.warn("{} {}/{}", new Object[]{message, host, port});
        }
    }
}
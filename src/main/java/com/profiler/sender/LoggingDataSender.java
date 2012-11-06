package com.profiler.sender;

import org.apache.thrift.TBase;

import java.util.logging.Logger;

/**
 *
 */
public class LoggingDataSender implements DataSender {
    private Logger logger = Logger.getLogger(this.getClass().getName());

    @Override
    public boolean send(TBase<?, ?> data) {
        logger.info("send tBase:" + data);
        return true;
    }

    @Override
    public void stop() {

    }
}

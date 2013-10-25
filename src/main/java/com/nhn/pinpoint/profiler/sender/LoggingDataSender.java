package com.nhn.pinpoint.profiler.sender;

import org.apache.thrift.TBase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 *
 */
public class LoggingDataSender implements EnhancedDataSender {

    public static final DataSender DEFAULT_LOGGING_DATA_SENDER = new LoggingDataSender();

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Override
    public boolean send(TBase<?, ?> data) {
        logger.info("send tBase:{}", data);
        return true;
    }


    @Override
    public void stop() {
        logger.info("LoggingDataSender stop");
    }

    @Override
    public boolean request(TBase<?, ?> data) {
        logger.info("request tBase:{}", data);
        return true;
    }

    @Override
    public boolean request(TBase<?, ?> data, int retry) {
        logger.info("request tBase:{} retry:{}", data, retry);
        return false;
    }
}

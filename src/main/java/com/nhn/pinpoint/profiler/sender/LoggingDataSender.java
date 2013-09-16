package com.nhn.pinpoint.profiler.sender;

import com.nhn.pinpoint.profiler.context.Thriftable;
import org.apache.thrift.TBase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 *
 */
public class LoggingDataSender implements DataSender {

    public static final DataSender DEFAULT_LOGGING_DATA_SENDER = new LoggingDataSender();

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Override
    public boolean send(TBase<?, ?> data) {
        logger.info("send tBase:{}", data);
        return true;
    }

    @Override
    public boolean send(Thriftable thriftable) {
        logger.info("send thriftable:{}", thriftable);
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
}

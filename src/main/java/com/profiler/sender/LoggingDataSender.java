package com.profiler.sender;

import com.profiler.context.Thriftable;
import org.apache.thrift.TBase;

import java.util.logging.Logger;

/**
 *
 */
public class LoggingDataSender implements DataSender {

    private static Logger logger = Logger.getLogger(LoggingDataSender.class.getName());
    public static DataSender DEFAULT_LOGGING_DATA_SENDER = new LoggingDataSender();

    @Override
    public boolean send(TBase<?, ?> data) {
        logger.info("send tBase:" + data);
        return true;
    }

    @Override
    public boolean send(Thriftable thriftable) {
        logger.info("send thriftable:" + thriftable);
        return true;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void stop() {
        logger.info("LoggingDataSender stop");
    }
}

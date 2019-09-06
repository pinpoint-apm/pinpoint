package com.navercorp.pinpoint.plugin.log4j2;

import com.navercorp.pinpoint.bootstrap.config.ProfilerConfig;

/**
 * @author licoco
 * @author King Jin
 */
public class Log4j2Config {

    static final String LOG4J2_LOGGING_TRANSACTION_INFO = "profiler.log4j2.logging.transactioninfo";

    private static final String ASYNC_LOGGER_TRANSFORM_CLASS = "profiler.log4j2.async.logging.transform.class";

    private final boolean log4j2LoggingTransactionInfo;

    private final String asyncLoggerTransformClass;

    Log4j2Config(ProfilerConfig config) {
        this.log4j2LoggingTransactionInfo = config.readBoolean(LOG4J2_LOGGING_TRANSACTION_INFO, false);
        this.asyncLoggerTransformClass = config.readString(ASYNC_LOGGER_TRANSFORM_CLASS, "org.apache.logging.log4j.core.async.AsyncLogger");
    }

    boolean isLog4j2LoggingTransactionInfo() {
        return log4j2LoggingTransactionInfo;
    }

    String getAsyncLoggerTransformClass() {
        return asyncLoggerTransformClass;
    }

    @Override
    public String toString() {
        return "Log4j2Config{" +
                "log4j2LoggingTransactionInfo=" + log4j2LoggingTransactionInfo +
                ", asyncLoggerTransformClass='" + asyncLoggerTransformClass + '\'' +
                '}';
    }
}

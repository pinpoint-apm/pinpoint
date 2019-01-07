package com.navercorp.pinpoint.plugin.log4j2;

import com.navercorp.pinpoint.bootstrap.config.ProfilerConfig;

/**
 * @Author: https://github.com/licoco/pinpoint
 * @Date: 2019/1/4 10:51
 * @Version: 1.0
 */
public class Log4j2Config {
    public static final String LOG4J2_LOGGING_TRANSACTION_INFO = "profiler.log4j2.logging.transactioninfo";

    private final boolean log4j2LoggingTransactionInfo;


    public Log4j2Config(ProfilerConfig config) {
        this.log4j2LoggingTransactionInfo = config.readBoolean(LOG4J2_LOGGING_TRANSACTION_INFO, false);
    }

    public boolean isLog4j2LoggingTransactionInfo() {
        return log4j2LoggingTransactionInfo;
    }

    @Override
    public String toString() {
        return "Log4jConfig [ log4j2LoggingTransactionInfo=" + log4j2LoggingTransactionInfo + "]";
    }

}

package com.navercorp.pinpoint.plugin.log4j2;

import com.navercorp.pinpoint.bootstrap.config.ProfilerConfig;

/**
 * @author licoco
 * @author King Jin
 */
public class Log4j2Config {

    static final String LOG4J2_LOGGING_TRANSACTION_INFO = "profiler.log4j2.logging.transactioninfo";

    private final boolean log4j2LoggingTransactionInfo;

    Log4j2Config(ProfilerConfig config) {
        this.log4j2LoggingTransactionInfo = config.readBoolean(LOG4J2_LOGGING_TRANSACTION_INFO, false);
    }

    boolean isLog4j2LoggingTransactionInfo() {
        return log4j2LoggingTransactionInfo;
    }

    @Override
    public String toString() {
        return "Log4jConfig [ log4j2LoggingTransactionInfo=" + log4j2LoggingTransactionInfo + "]";
    }

}

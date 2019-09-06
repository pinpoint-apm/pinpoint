package com.navercorp.pinpoint.plugin.log4j2;

import com.navercorp.pinpoint.bootstrap.config.ProfilerConfig;

/**
 * @author licoco
 * @author King Jin
 */
public class Log4j2Config {

    static final String LOG4J2_LOGGING_TRANSACTION_INFO = "profiler.log4j2.logging.transactioninfo";

    private static final String EXTENDED_LOGGER_IMPLEMENTATION_CLASS = "profiler.log4j2.logging.extended.logger.implementation";

    private final boolean log4j2LoggingTransactionInfo;

    private final String extendedLoggerImplementationClass;

    Log4j2Config(ProfilerConfig config) {
        this.log4j2LoggingTransactionInfo = config.readBoolean(LOG4J2_LOGGING_TRANSACTION_INFO, false);
        this.extendedLoggerImplementationClass = config.readString(EXTENDED_LOGGER_IMPLEMENTATION_CLASS, "org.apache.logging.log4j.core.Logger");
    }

    boolean isLog4j2LoggingTransactionInfo() {
        return log4j2LoggingTransactionInfo;
    }

    String getExtendedLoggerImplementationClass() {
        return extendedLoggerImplementationClass;
    }

    @Override
    public String toString() {
        return "Log4j2Config{" +
                "log4j2LoggingTransactionInfo=" + log4j2LoggingTransactionInfo +
                ", extendedLoggerImplementationClass='" + extendedLoggerImplementationClass + '\'' +
                '}';
    }
}

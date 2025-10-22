package com.navercorp.pinpoint.profiler.context.error;

import com.google.inject.Inject;
import com.navercorp.pinpoint.bootstrap.context.ErrorRecorder;
import com.navercorp.pinpoint.common.annotations.VisibleForTesting;
import com.navercorp.pinpoint.common.trace.ErrorCategory;
import com.navercorp.pinpoint.profiler.context.id.LocalTraceRoot;
import com.navercorp.pinpoint.profiler.context.config.ErrorRecorderConfig;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.EnumSet;
import java.util.Objects;

public class ConfigurableErrorRecorderFactory implements ErrorRecorderFactory {
    private static final Logger logger = LogManager.getLogger(ConfigurableErrorRecorderFactory.class);

    private final EnumSet<ErrorCategory> enabledTypes;

    @Inject
    public ConfigurableErrorRecorderFactory(ErrorRecorderConfig errorRecorderConfig) {
        Objects.requireNonNull(errorRecorderConfig, "errorRecorderConfig");
        this.enabledTypes = getEnabledTypes(errorRecorderConfig.getErrorMarkString());
    }

    @VisibleForTesting
    static EnumSet<ErrorCategory> getEnabledTypes(String errorMarkString) {
        if (errorMarkString == null) {
            return EnumSet.allOf(ErrorCategory.class);
        }

        EnumSet<ErrorCategory> enabledTypes = EnumSet.of(ErrorCategory.UNKNOWN);

        for (String category : errorMarkString.split(",")) {
            switch (category.trim().toLowerCase()) {
                case "exception":
                    enabledTypes.add(ErrorCategory.EXCEPTION);
                    break;
                case "http-status":
                    enabledTypes.add(ErrorCategory.HTTP_STATUS);
                    break;
                case "sql":
                    enabledTypes.add(ErrorCategory.SQL);
                    break;
                default:
                    logger.warn("Unknown error category string: {}", category);
                    break;
            }
        }

        return enabledTypes;
    }

    @Override
    public ErrorRecorder newRecorder(LocalTraceRoot traceRoot) {
        return new ConfigurableErrorRecorder(traceRoot, enabledTypes);
    }
}

package com.navercorp.pinpoint.profiler.context.error;

import com.google.inject.Inject;
import com.navercorp.pinpoint.bootstrap.context.ErrorRecorder;
import com.navercorp.pinpoint.common.annotations.VisibleForTesting;
import com.navercorp.pinpoint.common.trace.ErrorCategory;
import com.navercorp.pinpoint.profiler.context.config.ErrorRecorderConfig;
import com.navercorp.pinpoint.profiler.context.id.LocalTraceRoot;
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
        this.enabledTypes = getEnabledTypes(errorRecorderConfig.getErrorMarkString(), errorRecorderConfig.getErrorMarkExcludeString());
    }

    @VisibleForTesting
    static EnumSet<ErrorCategory> getEnabledTypes(String errorMarkString, String errorMarkExcludeString) {
        EnumSet<ErrorCategory> mark = errorMarkString != null ? toCategorySet(errorMarkString) : EnumSet.allOf(ErrorCategory.class);
        EnumSet<ErrorCategory> exclude = errorMarkExcludeString != null ? toCategorySet(errorMarkExcludeString) : EnumSet.noneOf(ErrorCategory.class);

        mark.removeAll(exclude);
        mark.add(ErrorCategory.UNKNOWN);

        return mark;
    }

    private static EnumSet<ErrorCategory> toCategorySet(String categoryString) {
        EnumSet<ErrorCategory> result = EnumSet.noneOf(ErrorCategory.class);

        for (String category : categoryString.split(",")) {
            switch (category.trim().toLowerCase()) {
                case "exception":
                    result.add(ErrorCategory.EXCEPTION);
                    break;
                case "http-status":
                    result.add(ErrorCategory.HTTP_STATUS);
                    break;
                case "sql":
                    result.add(ErrorCategory.SQL);
                    break;
                case "":
                    break;
                default:
                    logger.warn("Invalid error category string: {}", category);
                    break;
            }
        }
        return result;
    }

    @Override
    public ErrorRecorder newRecorder(LocalTraceRoot traceRoot) {
        return new ConfigurableErrorRecorder(traceRoot, enabledTypes);
    }
}

package com.navercorp.pinpoint.profiler.context.error;

import com.navercorp.pinpoint.bootstrap.context.ErrorRecorder;
import com.navercorp.pinpoint.common.trace.ErrorCategory;
import com.navercorp.pinpoint.profiler.context.id.LocalTraceRoot;

import java.util.EnumSet;
import java.util.Objects;

public class ConfigurableErrorRecorderFactory implements ErrorRecorderFactory {
    private final EnumSet<ErrorCategory> enabledTypes;

    public ConfigurableErrorRecorderFactory(ErrorRecorderConfig errorRecorderConfig) {
        Objects.requireNonNull(errorRecorderConfig, "errorRecorderConfig");
        this.enabledTypes = getEnabledTypes(errorRecorderConfig.getErrorMarkString());
    }

    private static EnumSet<ErrorCategory> getEnabledTypes(String errorMarkString) {
        EnumSet<ErrorCategory> enabledTypes = EnumSet.of(ErrorCategory.UNKNOWN);

        if (errorMarkString == null || errorMarkString.trim().isEmpty()) {
            return enabledTypes;
        }

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
            }
        }

        return enabledTypes;
    }

    @Override
    public ErrorRecorder newRecorder(LocalTraceRoot traceRoot) {
        return new ConfigurableErrorRecorder(traceRoot, enabledTypes);
    }
}

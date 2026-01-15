package com.navercorp.pinpoint.profiler.context.error;

import com.navercorp.pinpoint.bootstrap.context.ErrorRecorder;
import com.navercorp.pinpoint.common.trace.ErrorCategory;
import com.navercorp.pinpoint.profiler.context.id.LocalTraceRoot;

import java.util.Objects;
import java.util.Set;

public class ConfigurableErrorRecorder implements ErrorRecorder {
    private final LocalTraceRoot traceRoot;
    private final Set<ErrorCategory> enabledCategories;

    public ConfigurableErrorRecorder(LocalTraceRoot localTraceRoot, Set<ErrorCategory> enabledCategories) {
        this.traceRoot = Objects.requireNonNull(localTraceRoot, "localTraceRoot");
        this.enabledCategories = Objects.requireNonNull(enabledCategories, "enabledCategories");
    }

    @Override
    public void recordError(ErrorCategory errorCategory) {
        if (enabledCategories.contains(errorCategory)) {
            traceRoot.getShared().maskErrorCode(errorCategory.getBitMask());
        }
    }
}

package com.navercorp.pinpoint.profiler.context.error;

import com.navercorp.pinpoint.common.config.Value;

public class ErrorRecorderConfig {
    @Value("${profiler.error.mark}")
    private String errorMarkString;

    public String getErrorMarkString() {
        return errorMarkString;
    }
}

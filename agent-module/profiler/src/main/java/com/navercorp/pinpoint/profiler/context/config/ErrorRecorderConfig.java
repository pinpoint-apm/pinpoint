package com.navercorp.pinpoint.profiler.context.config;

import com.navercorp.pinpoint.common.config.Value;

public class ErrorRecorderConfig {
    @Value("${profiler.error.enable}")
    private boolean enable = true;

    @Value("${profiler.error.mark}")
    private String errorMarkString;

    public boolean isEnable() {
        return enable;
    }

    public String getErrorMarkString() {
        return errorMarkString;
    }
}

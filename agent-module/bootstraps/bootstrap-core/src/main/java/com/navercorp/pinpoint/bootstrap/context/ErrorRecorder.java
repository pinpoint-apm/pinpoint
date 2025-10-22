package com.navercorp.pinpoint.bootstrap.context;

import com.navercorp.pinpoint.common.trace.ErrorCategory;

public interface ErrorRecorder {
    void recordError(ErrorCategory errorCategory);
}

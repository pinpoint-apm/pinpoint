package com.navercorp.pinpoint.bootstrap.plugin.request;

import com.navercorp.pinpoint.bootstrap.context.SpanRecorder;

public class BypassServerHeaderRecorder<T> implements ServerHeaderRecorder<T> {
    @Override
    public void recordHeader(SpanRecorder recorder, T request) {
        // empty
    }
}

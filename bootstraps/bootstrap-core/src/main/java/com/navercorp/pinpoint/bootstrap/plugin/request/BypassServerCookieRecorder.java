package com.navercorp.pinpoint.bootstrap.plugin.request;

import com.navercorp.pinpoint.bootstrap.context.SpanRecorder;

public class BypassServerCookieRecorder<T> implements ServerCookieRecorder<T> {
    @Override
    public void recordCookie(SpanRecorder recorder, T request) {
        // empty
    }
}

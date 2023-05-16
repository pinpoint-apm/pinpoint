package com.navercorp.pinpoint.bootstrap.plugin.request;

import com.navercorp.pinpoint.bootstrap.context.SpanRecorder;

public interface ServerCookieRecorder<REQ> {
    String CONFIG_KEY_RECORD_REQ_COOKIES = "profiler.http.record.request.cookies";

    void recordCookie(SpanRecorder recorder, REQ request);
}

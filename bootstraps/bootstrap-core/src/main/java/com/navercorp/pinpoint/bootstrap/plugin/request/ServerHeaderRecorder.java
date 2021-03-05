package com.navercorp.pinpoint.bootstrap.plugin.request;

import com.navercorp.pinpoint.bootstrap.context.SpanRecorder;

public interface ServerHeaderRecorder<REQ> {
    String CONFIG_KEY_RECORD_REQ_HEADERS = "profiler.http.record.request.headers";

    void recordHeader(SpanRecorder recorder, REQ request);
}

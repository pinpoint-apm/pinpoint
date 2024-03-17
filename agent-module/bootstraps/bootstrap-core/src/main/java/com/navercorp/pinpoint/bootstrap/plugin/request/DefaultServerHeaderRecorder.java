package com.navercorp.pinpoint.bootstrap.plugin.request;

import com.navercorp.pinpoint.bootstrap.context.SpanRecorder;
import com.navercorp.pinpoint.common.trace.AnnotationKey;
import com.navercorp.pinpoint.common.util.StringStringValue;

import java.util.List;
import java.util.Objects;

public class DefaultServerHeaderRecorder<REQ> implements ServerHeaderRecorder<REQ> {

    private final RequestAdaptor<REQ> requestAdaptor;
    private final String[] recordHeaders;

    public DefaultServerHeaderRecorder(RequestAdaptor<REQ> requestAdaptor, List<String> recordHeaders) {
        this.requestAdaptor = Objects.requireNonNull(requestAdaptor, "requestAdaptor");
        Objects.requireNonNull(recordHeaders, "recordHeaders");
        this.recordHeaders = recordHeaders.toArray(new String[0]);
    }

    @Override
    public void recordHeader(final SpanRecorder recorder, final REQ request) {

        for (String headerName : recordHeaders) {
            final String value = requestAdaptor.getHeader(request, headerName);
            if (value == null) {
                continue;
            }
            StringStringValue header = new StringStringValue(headerName, value);
            recorder.recordAttribute(AnnotationKey.HTTP_REQUEST_HEADER, header);
        }
    }
}

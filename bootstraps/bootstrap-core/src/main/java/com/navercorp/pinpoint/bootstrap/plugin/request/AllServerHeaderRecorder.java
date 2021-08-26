package com.navercorp.pinpoint.bootstrap.plugin.request;

import com.navercorp.pinpoint.bootstrap.context.SpanRecorder;
import com.navercorp.pinpoint.bootstrap.logging.PLogger;
import com.navercorp.pinpoint.bootstrap.logging.PLoggerFactory;
import com.navercorp.pinpoint.common.trace.AnnotationKey;
import com.navercorp.pinpoint.common.util.StringStringValue;

import java.util.Collection;
import java.util.Collections;
import java.util.Objects;

public class AllServerHeaderRecorder<REQ> implements ServerHeaderRecorder<REQ> {

    private final PLogger logger = PLoggerFactory.getLogger(this.getClass());

    private final RequestAdaptor<REQ> requestAdaptor;

    public AllServerHeaderRecorder(RequestAdaptor<REQ> requestAdaptor) {
        this.requestAdaptor = Objects.requireNonNull(requestAdaptor, "requestAdaptor");
    }

    @Override
    public void recordHeader(final SpanRecorder recorder, final REQ request) {
        final Collection<String> headerNames = getHeaderNames(request);

        for (String headerName : headerNames) {
            final String value = requestAdaptor.getHeader(request, headerName);
            if (value == null) {
                continue;
            }
            StringStringValue header = new StringStringValue(headerName, value);
            recorder.recordAttribute(AnnotationKey.HTTP_REQUEST_HEADER, header);
        }
    }

    private Collection<String> getHeaderNames(final REQ request) {
        try {
            // It's good that APM dumps duplicate headers.
            return requestAdaptor.getHeaderNames(request);
        } catch (Exception e) {
            logger.warn("Extract all of the request header names from request {} failed, caused by:", request, e);
            return Collections.emptyList();
        }
    }

}

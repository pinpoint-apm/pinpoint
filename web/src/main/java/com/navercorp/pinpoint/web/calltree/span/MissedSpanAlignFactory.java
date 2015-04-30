package com.navercorp.pinpoint.web.calltree.span;

import java.util.ArrayList;
import java.util.List;

import com.navercorp.pinpoint.common.bo.AnnotationBo;
import com.navercorp.pinpoint.common.bo.ApiMetaDataBo;
import com.navercorp.pinpoint.common.bo.SpanBo;
import com.navercorp.pinpoint.common.bo.SpanEventBo;
import com.navercorp.pinpoint.common.trace.AnnotationKey;
import com.navercorp.pinpoint.common.trace.ServiceType;

public class MissedSpanAlignFactory {
    // private static final long DEFAULT_TIMEOUT_MILLISEC = 10 * 60 * 1000;
    private static final long DEFAULT_TIMEOUT_MILLISEC = 60 * 1000;

    private long timeoutMillisec = DEFAULT_TIMEOUT_MILLISEC;

    public SpanAlign get(final int depth, final SpanBo span, final SpanEventBo spanEvent) {
        final SpanEventBo missedEvent = new SpanEventBo();
        missedEvent.setStartElapsed(spanEvent.getStartElapsed());
        missedEvent.setEndElapsed(spanEvent.getEndElapsed());
        missedEvent.setAgentId(spanEvent.getAgentId());
        missedEvent.setAgentStartTime(spanEvent.getAgentStartTime());
        missedEvent.setServiceType(ServiceType.COLLECTOR.getCode());

        List<AnnotationBo> annotations = new ArrayList<AnnotationBo>();

        ApiMetaDataBo apiMetaData = new ApiMetaDataBo();
        apiMetaData.setLineNumber(-1);
        apiMetaData.setApiInfo("...");
        apiMetaData.setType(3);

        final AnnotationBo apiMetaDataAnnotation = new AnnotationBo();
        apiMetaDataAnnotation.setKey(AnnotationKey.API_METADATA.getCode());
        apiMetaDataAnnotation.setValue(apiMetaData);
        annotations.add(apiMetaDataAnnotation);

        final AnnotationBo argumentAnnotation = new AnnotationBo();
        argumentAnnotation.setKey(AnnotationKey.getArgs(0).getCode());
        if (System.currentTimeMillis() - span.getStartTime() < timeoutMillisec) {
            argumentAnnotation.setValue("WAITING FOR PACKET ");
        } else {
            argumentAnnotation.setValue("PACKET LOSS");
        }
        annotations.add(argumentAnnotation);

        missedEvent.setAnnotationBoList(annotations);

        return new SpanAlign(span, missedEvent);
    }
}
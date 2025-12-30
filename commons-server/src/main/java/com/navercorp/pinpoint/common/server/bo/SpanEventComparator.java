package com.navercorp.pinpoint.common.server.bo;

import com.navercorp.pinpoint.common.trace.AnnotationKey;

import java.util.Comparator;

/**
 * @author Woonduk Kang(emeroad)
 */
public final class SpanEventComparator {

    public static final Comparator<SpanEventBo> INSTANCE = Comparator.comparingInt(SpanEventBo::getSequence);
    public static final Comparator<SpanEventBo> ANNOTATION_START_TIME = Comparator.comparingLong(spanEventBo -> {
        for(AnnotationBo annotationBo : spanEventBo.getAnnotationBoList()) {
            if(AnnotationKey.OPENTELEMETRY_START_TIME.getCode() == annotationBo.getKey()) {
                return (long) annotationBo.getValue();
            }
        }
        return 0;
    });
}

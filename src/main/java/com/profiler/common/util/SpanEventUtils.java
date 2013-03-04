package com.profiler.common.util;

import com.profiler.common.AnnotationKey;
import com.profiler.common.dto.thrift.Annotation;
import com.profiler.common.dto.thrift.SpanEvent;

import java.util.List;

/**
 *
 */
public class SpanEventUtils {

    public static boolean hasException(SpanEvent spanEvent) {
        final List<Annotation> annotations = spanEvent.getAnnotations();
        if (annotations == null) {
            return true;
        }

        for (Annotation annotation : annotations) {
            // 나중에 레인지 체크로 변경되어야 할 가능성이 있음.
            if (annotation.getKey() == AnnotationKey.EXCEPTION.getCode()) {
                return true;
            }
        }
        return false;
    }
}

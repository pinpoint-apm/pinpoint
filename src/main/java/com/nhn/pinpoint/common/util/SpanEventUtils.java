package com.nhn.pinpoint.common.util;

import com.nhn.pinpoint.common.AnnotationKey;
import com.nhn.pinpoint.thrift.dto.TAnnotation;
import com.nhn.pinpoint.thrift.dto.TSpanEvent;

import java.util.List;

/**
 *
 */
public class SpanEventUtils {

    public static boolean hasException(TSpanEvent spanEvent) {
        final List<TAnnotation> annotations = spanEvent.getAnnotations();
        if (annotations == null) {
            return true;
        }

        for (TAnnotation annotation : annotations) {
            // 나중에 레인지 체크로 변경되어야 할 가능성이 있음.
            if (annotation.getKey() == AnnotationKey.EXCEPTION.getCode()) {
                return true;
            }
        }
        return false;
    }
}

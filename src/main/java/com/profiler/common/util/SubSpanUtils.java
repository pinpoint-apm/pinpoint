package com.profiler.common.util;

import com.profiler.common.AnnotationKey;
import com.profiler.common.dto.thrift.Annotation;
import com.profiler.common.dto.thrift.SubSpan;

import java.util.List;

/**
 *
 */
public class SubSpanUtils {

    public static boolean hasException(SubSpan subSpan) {
        final List<Annotation> annotations = subSpan.getAnnotations();
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

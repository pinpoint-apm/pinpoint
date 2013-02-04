package com.profiler.common.util;

import com.profiler.common.dto.thrift.Annotation;

/**
 *
 */
public class AnnotationTypeUtils {

    public static int getType(Annotation annotation) {
//        if(annotation.isSetStringValue()) {
//            return 0;
//        } else if(annotation.isSetIntValue()) {
//            return 1;
//        } else if(annotation.isSetLongValue()) {
//            return 2;
//        } else if(annotation.isSetShortValue()) {
//            return 3;
//        }
        return -1;
    }
}

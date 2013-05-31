package com.nhn.pinpoint.common.util;

import com.nhn.pinpoint.common.dto2.thrift.Annotation;

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

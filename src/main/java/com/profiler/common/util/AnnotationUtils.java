package com.profiler.common.util;

import java.text.SimpleDateFormat;
import java.util.*;

import com.profiler.common.AnnotationKey;
import com.profiler.common.ServiceType;
import com.profiler.common.bo.AnnotationBo;
import com.profiler.common.bo.Span;

public class AnnotationUtils {

    private static final String FORMAT = "yyyy-MM-dd HH:mm:ss SSS";

    public static String longToDateStr(long date, String fmt) {
        SimpleDateFormat format = new SimpleDateFormat((fmt == null) ? FORMAT : fmt);
        return format.format(new Date(date));
    }

    public static String findApiAnnotation(List<AnnotationBo> list) {
        AnnotationBo annotationBo = findAnnotationBo(list, AnnotationKey.API);
        if (annotationBo != null) {
            return (String) annotationBo.getValue();
        }
        return null;
    }

    public static AnnotationBo findAnnotationBo(List<AnnotationBo> annotationBoList, AnnotationKey annotationKey) {
        for (AnnotationBo annotation : annotationBoList) {
            int key = annotation.getKey();
            if (annotationKey.getCode() == key) {
                return annotation;
            }
        }
        return null;
    }

    public static AnnotationBo getDisplayArgument(Span span) {
        // arcus 관련 일반화 필요.
        List<AnnotationBo> list = span.getAnnotationBoList();
        if (span.getServiceType() == ServiceType.ARCUS || span.getServiceType() == ServiceType.MEMCACHED) {
            return findAnnotationBo(list, AnnotationKey.ARCUS_COMMAND);
        }

        // rpc connector의 경우 보여주는 code일반화 필요.
        if (span.getServiceType() == ServiceType.HTTP_CLIENT || span.getServiceType() == ServiceType.JDK_HTTPURLCONNECTOR) {
            return findAnnotationBo(list, AnnotationKey.HTTP_URL);
        }


//        span에 해당하는 Tomcat의 경우 Span에 포함된 rpc 필드를 사용하므로 annotation에서 찾을필요가 없음.
//        if (span.getServiceType() == ServiceType.TOMCAT) {
//            return findAnnotationBo(list, AnnotationKey.HTTP_URL);
//        }
//

        if (span.getServiceType() == ServiceType.MYSQL) {
            return findAnnotationBo(list, AnnotationKey.ARGS0);
        }
        return null;
    }

    private static  List<AnnotationKey> API_META_DATA_ERROR;
    static {
        API_META_DATA_ERROR = loadApiMetaDataError();
    }

    static List<AnnotationKey> loadApiMetaDataError() {
        List<AnnotationKey> apiMetaData = new ArrayList<AnnotationKey>();
        for (AnnotationKey annotationKey : AnnotationKey.values()) {
            if (annotationKey.name().startsWith("ERROR_API_METADATA_")) {
                apiMetaData.add(annotationKey);
            }
        }
         return apiMetaData;
    }

    public static AnnotationKey getApiMetaDataError(List<AnnotationBo> annotationBoList) {
        for (AnnotationBo bo : annotationBoList) {
            AnnotationKey apiErrorCode = findApiErrorCode(bo);
            if (apiErrorCode != null) {
                return apiErrorCode;
            }
        }
        // 정확한 에러 코드를 못찾음. 퉁쳐서 에러 처리
        return AnnotationKey.ERROR_API_METADATA_ERROR;
    }

    private static AnnotationKey findApiErrorCode(AnnotationBo bo) {
        for (AnnotationKey annotationKey : API_META_DATA_ERROR) {
            if (bo.getKey() == annotationKey.getCode()) {
                return annotationKey;
            }
        }
        return null;
    }

}

package com.nhn.hippo.web.utils;

import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import com.profiler.common.ServiceType;
import com.profiler.common.bo.AnnotationBo;
import com.profiler.common.bo.Span;

public class AnnotationUtils {

    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss SSS");

    private static final String API = "API";
    private static final String ARCUS_COMMAND = "arcus.command";
    private static final String HTTP_URL = "http.url";

    @Deprecated
    public static String longToDateStr(long date) {
        // thread safe하지 않음
        return dateFormat.format(new Date(date));
    }

    public static String longLongToUUID(long mostTraceId, long leastTraceId) {
        return new UUID(mostTraceId, leastTraceId).toString();
    }

    private static Comparator<AnnotationBo> annotationKeyComparator = new Comparator<AnnotationBo>() {
        @Override
        public int compare(AnnotationBo a1, AnnotationBo a2) {
            return a1.getKey().compareTo(a2.getKey());
        }
    };

    public static void sortAnnotationListByKey(Span span) {
        List<AnnotationBo> list = span.getAnnotationBoList();
        Collections.sort(list, annotationKeyComparator);
    }

    public static Object getDisplayMethod(Span span) {
        List<AnnotationBo> list = span.getAnnotationBoList();
        int index = Collections.binarySearch(list, API);

        if (index > -1) {
            return list.get(index).getValue();
        }

        if (span.getServiceType() == ServiceType.ARCUS || span.getServiceType() == ServiceType.MEMCACHED) {
            return span.getRpc(); // return OperationImpl
        }

        return null;
    }

    public static Object getDisplayArgument(Span span) {
        List<AnnotationBo> list = span.getAnnotationBoList();
        int index = -1;
        // TODO BinarySearch는 collection이 정렬되어 있어야 올바른 값을 찾을수 있음. 오동작할것으로 보임.
        if (span.getServiceType() == ServiceType.ARCUS || span.getServiceType() == ServiceType.MEMCACHED) {
            index = Collections.binarySearch(list, ARCUS_COMMAND);
        }

        if (span.getServiceType() == ServiceType.HTTP_CLIENT) {
            index = Collections.binarySearch(list, HTTP_URL);
        }

        if (span.getServiceType() == ServiceType.TOMCAT) {
            index = Collections.binarySearch(list, HTTP_URL);
        }

        if (index > -1) {
            return list.get(index).getValue();
        }

        return null;
    }
}

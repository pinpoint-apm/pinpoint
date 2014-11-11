package com.nhn.pinpoint.common.util;

import java.util.*;

import com.nhn.pinpoint.common.AnnotationKey;
import com.nhn.pinpoint.common.ServiceType;
import com.nhn.pinpoint.common.bo.AnnotationBo;
import com.nhn.pinpoint.common.bo.Span;

/**
 * @author emeroad
 */
public class AnnotationUtils {

    public static String findApiAnnotation(List<AnnotationBo> list) {
        if (list == null) {
            return null;
        }
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

    public static AnnotationBo findArgsAnnotationBo(List<AnnotationBo> annotationBoList) {
        for (AnnotationBo annotation : annotationBoList) {
            if (AnnotationKey.isArgsKey(annotation.getKey())) {
                return annotation;
            }
        }
        return null;
    }

    public static AnnotationBo getDisplayArgument(Span span) {
        // arcus 관련 일반화 필요.
        List<AnnotationBo> list = span.getAnnotationBoList();
        if (list == null) {
            return null;
        }
        final ServiceType serviceType = span.getServiceType();
        if (serviceType == ServiceType.ARCUS || serviceType == ServiceType.MEMCACHED) {
            // 첫번째 args아무거나 하나를 디스플레이에 뿌린다.
            // TODO 2개 이상일 경우의 케이스 일때 비기는 하나, 현재 arucs쪽 파라미터 덤프키는 일단 1개뿐이라 괜찮을듯하다.
            return findArgsAnnotationBo(list);
        }

        // rpc connector의 경우 보여주는 code일반화 필요.
        if (serviceType == ServiceType.HTTP_CLIENT || serviceType == ServiceType.JDK_HTTPURLCONNECTOR) {
            return findAnnotationBo(list, AnnotationKey.HTTP_URL);
        }
        
        if (serviceType == ServiceType.HTTP_CLIENT_INTERNAL) {
            return findAnnotationBo(list, AnnotationKey.HTTP_CALL_RETRY_COUNT);
        }

//        span에 해당하는 Tomcat의 경우 Span에 포함된 rpc 필드를 사용하므로 annotation에서 찾을필요가 없음.
//        if (span.getServiceType() == ServiceType.TOMCAT) {
//            return findAnnotationBo(list, AnnotationKey.HTTP_URL);
//        }
//
        // TODO 먼가 고쳐야 함.
        if (serviceType == ServiceType.MYSQL || serviceType == ServiceType.MYSQL_EXECUTE_QUERY
                || serviceType == ServiceType.ORACLE || serviceType == ServiceType.ORACLE_EXECUTE_QUERY
                || serviceType == ServiceType.MSSQL || serviceType == ServiceType.MSSQL_EXECUTE_QUERY
                || serviceType == ServiceType.CUBRID || serviceType == ServiceType.CUBRID_EXECUTE_QUERY) {
            // args 0의 경우 연결string이다
            // 구현 방법이 매우 구림 좀더 개선 필요.
            return findAnnotationBo(list, AnnotationKey.ARGS0);
        }
        
        if (serviceType == ServiceType.IBATIS || serviceType == ServiceType.MYBATIS) {
        	return findAnnotationBo(list, AnnotationKey.ARGS0);
        }
        
        if (serviceType == ServiceType.SPRING_ORM_IBATIS) {
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

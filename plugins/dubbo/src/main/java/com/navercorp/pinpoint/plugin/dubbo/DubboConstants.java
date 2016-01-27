package com.navercorp.pinpoint.plugin.dubbo;

import com.navercorp.pinpoint.common.trace.AnnotationKey;
import com.navercorp.pinpoint.common.trace.AnnotationKeyFactory;
import com.navercorp.pinpoint.common.trace.ServiceType;
import com.navercorp.pinpoint.common.trace.ServiceTypeFactory;

import static com.navercorp.pinpoint.common.trace.ServiceTypeProperty.RECORD_STATISTICS;

/**
 * @author Jinkai.Ma
 */
public interface DubboConstants {

    ServiceType DUBBO_PROVIDER_SERVICE_TYPE = ServiceTypeFactory.of(1110, "DUBBO_PROVIDER", RECORD_STATISTICS);
    ServiceType DUBBO_CONSUMER_SERVICE_TYPE = ServiceTypeFactory.of(9110, "DUBBO_CONSUMER", RECORD_STATISTICS);
    AnnotationKey DUBBO_ARGS_ANNOTATION_KEY = AnnotationKeyFactory.of(90, "dubbo.args");
    AnnotationKey DUBBO_RESULT_ANNOTATION_KEY = AnnotationKeyFactory.of(91, "dubbo.result");

    String META_DO_NOT_TRACE = "_DUBBO_DO_NOT_TRACE";
    String META_TRANSACTION_ID = "_DUBBO_TRASACTION_ID";
    String META_SPAN_ID = "_DUBBO_SPAN_ID";
    String META_PARENT_SPAN_ID = "_DUBBO_PARENT_SPAN_ID";
    String META_PARENT_APPLICATION_NAME = "_DUBBO_PARENT_APPLICATION_NAME";
    String META_PARENT_APPLICATION_TYPE = "_DUBBO_PARENT_APPLICATION_TYPE";
    String META_FLAGS = "_DUBBO_FLAGS";

    String MONITOR_SERVICE_FQCN = "com.alibaba.dubbo.monitor.MonitorService";
}

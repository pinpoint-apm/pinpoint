package com.navercorp.pinpoint.plugin.openwhisk;

import com.navercorp.pinpoint.common.trace.*;

import static com.navercorp.pinpoint.common.trace.AnnotationKeyProperty.VIEW_IN_RECORD_SET;
import static com.navercorp.pinpoint.common.trace.ServiceTypeProperty.RECORD_STATISTICS;
import static com.navercorp.pinpoint.common.trace.ServiceTypeProperty.INCLUDE_DESTINATION_ID;
import static com.navercorp.pinpoint.common.trace.ServiceTypeProperty.TERMINAL;

/**
 * @author upgle (Seonghyun, Oh)
 */
public class OpenwhiskConstants {
    public static final ServiceType OPENWHISK_INTERNAL = ServiceTypeFactory.of(1920, "OPENWHISK_INTERNAL", "OPENWHISK_INTERNAL");
    public static final ServiceType OPENWHISK_CONTROLLER = ServiceTypeFactory.of(1921, "OPENWHISK_CONTROLLER", "OPENWHISK_CONTROLLER", ServiceTypeProperty.RECORD_STATISTICS);
    public static final ServiceType OPENWHISK_INVOKER = ServiceTypeFactory.of(1922, "OPENWHISK_INVOKER", "OPENWHISK_INVOKER", ServiceTypeProperty.RECORD_STATISTICS);

    public static final ServiceType COUCHDB = ServiceTypeFactory.of(2900, "COUCHDB", TERMINAL, INCLUDE_DESTINATION_ID);
    public static final ServiceType COUCHDB_EXECUTE_QUERY = ServiceTypeFactory.of(2901, "COUCHDB_EXECUTE_QUERY", "COUCHDB", TERMINAL, RECORD_STATISTICS, INCLUDE_DESTINATION_ID);

    public static final AnnotationKey MARKER_MESSAGE = AnnotationKeyFactory.of(923, "marker.message", VIEW_IN_RECORD_SET);

    public static final String CALLER = "CALLER";
}

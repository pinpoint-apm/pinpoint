/*
 * Copyright 2018 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.navercorp.pinpoint.plugin.openwhisk;

import com.navercorp.pinpoint.common.trace.*;

import static com.navercorp.pinpoint.common.trace.AnnotationKeyProperty.VIEW_IN_RECORD_SET;
import static com.navercorp.pinpoint.common.trace.ServiceTypeProperty.RECORD_STATISTICS;
import static com.navercorp.pinpoint.common.trace.ServiceTypeProperty.INCLUDE_DESTINATION_ID;
import static com.navercorp.pinpoint.common.trace.ServiceTypeProperty.TERMINAL;

/**
 * @author Seonghyun Oh
 */
public class OpenwhiskConstants {
    public static final ServiceType OPENWHISK_INTERNAL = ServiceTypeFactory.of(1620, "OPENWHISK_INTERNAL", "OPENWHISK_INTERNAL");
    public static final ServiceType OPENWHISK_CONTROLLER = ServiceTypeFactory.of(1621, "OPENWHISK_CONTROLLER", "OPENWHISK_CONTROLLER", ServiceTypeProperty.RECORD_STATISTICS);
    public static final ServiceType OPENWHISK_INVOKER = ServiceTypeFactory.of(1622, "OPENWHISK_INVOKER", "OPENWHISK_INVOKER", ServiceTypeProperty.RECORD_STATISTICS);
    public static final ServiceType OPENWHISK_CLIENT = ServiceTypeFactory.of(9622, "OPENWHISK_CLIENT", "OPENWHISK_CLIENT", ServiceTypeProperty.RECORD_STATISTICS);

    public static final ServiceType COUCHDB = ServiceTypeFactory.of(2700, "COUCHDB", TERMINAL, INCLUDE_DESTINATION_ID);
    public static final ServiceType COUCHDB_EXECUTE_QUERY = ServiceTypeFactory.of(2701, "COUCHDB_EXECUTE_QUERY", "COUCHDB", TERMINAL, RECORD_STATISTICS, INCLUDE_DESTINATION_ID);

    public static final AnnotationKey MARKER_MESSAGE = AnnotationKeyFactory.of(923, "marker.message", VIEW_IN_RECORD_SET);
}

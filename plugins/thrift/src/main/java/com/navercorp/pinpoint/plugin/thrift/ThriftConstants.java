/*
 * Copyright 2015 NAVER Corp.
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

package com.navercorp.pinpoint.plugin.thrift;

import static com.navercorp.pinpoint.common.trace.ServiceTypeProperty.*;

import java.util.regex.Pattern;

import com.navercorp.pinpoint.common.trace.AnnotationKey;
import com.navercorp.pinpoint.common.trace.AnnotationKeyFactory;
import com.navercorp.pinpoint.common.trace.AnnotationKeyProperty;
import com.navercorp.pinpoint.common.trace.ServiceType;
import com.navercorp.pinpoint.common.trace.ServiceTypeFactory;

/**
 * @author HyunGil Jeong
 */
public final class ThriftConstants {
    private ThriftConstants() {
    }

    public static final ServiceType THRIFT_SERVER = ServiceTypeFactory.of(1100, "THRIFT_SERVER", RECORD_STATISTICS);
    public static final ServiceType THRIFT_CLIENT = ServiceTypeFactory.of(9100, "THRIFT_CLIENT", RECORD_STATISTICS);
    public static final ServiceType THRIFT_SERVER_INTERNAL = ServiceTypeFactory.of(1101, "THRIFT_SERVER_INTERNAL", "THRIFT_SERVER");
    public static final ServiceType THRIFT_CLIENT_INTERNAL = ServiceTypeFactory.of(9101, "THRIFT_CLIENT_INTERNAL", "THRIFT_CLIENT");

    public static final AnnotationKey THRIFT_URL = AnnotationKeyFactory.of(80, "thrift.url");
    public static final AnnotationKey THRIFT_ARGS = AnnotationKeyFactory.of(81, "thrift.args", AnnotationKeyProperty.VIEW_IN_RECORD_SET);
    public static final AnnotationKey THRIFT_RESULT = AnnotationKeyFactory.of(82, "thrift.result", AnnotationKeyProperty.VIEW_IN_RECORD_SET);

    public static final String UNKNOWN_METHOD_NAME = "unknown";
    public static final String UNKNOWN_METHOD_URI = "/" + UNKNOWN_METHOD_NAME;
    public static final String UNKNOWN_ADDRESS = "Unknown";

    public static final Pattern PROCESSOR_PATTERN = Pattern.compile("\\$Processor");
    public static final Pattern ASYNC_PROCESSOR_PATTERN = Pattern.compile("\\$AsyncProcessor");
    public static final Pattern CLIENT_PATTERN = Pattern.compile("\\$Client");
    public static final Pattern ASYNC_METHOD_CALL_PATTERN = Pattern.compile("\\$AsyncClient\\$");

    // field names
    public static final String T_ASYNC_METHOD_CALL_FIELD_TRANSPORT = "transport";
    public static final String FRAME_BUFFER_FIELD_TRANS_ = "trans_";
    public static final String FRAME_BUFFER_FIELD_IN_TRANS_ = "inTrans_";

    // custom field injector (accessor) FQCN
    private static final String FIELD_ACCESSOR_BASE = "com.navercorp.pinpoint.plugin.thrift.field.accessor.";
    public static final String FIELD_ACCESSOR_ASYNC_MARKER_FLAG = FIELD_ACCESSOR_BASE + "AsyncMarkerFlagFieldAccessor";
    public static final String FIELD_ACCESSOR_SERVER_MARKER_FLAG = FIELD_ACCESSOR_BASE + "ServerMarkerFlagFieldAccessor";
    public static final String FIELD_ACCESSOR_SOCKET_ADDRESS = FIELD_ACCESSOR_BASE + "SocketAddressFieldAccessor";
    public static final String FIELD_ACCESSOR_SOCKET = FIELD_ACCESSOR_BASE + "SocketFieldAccessor";

    // field getter FQCN
    private static final String FIELD_GETTER_BASE = "com.navercorp.pinpoint.plugin.thrift.field.getter.";
    public static final String FIELD_GETTER_T_NON_BLOCKING_TRANSPORT = FIELD_GETTER_BASE + "TNonblockingTransportFieldGetter";
    public static final String FIELD_GETTER_T_TRANSPORT = FIELD_GETTER_BASE + "TTransportFieldGetter";
    public static final String FIELD_GETTER_T_PROTOCOL = FIELD_GETTER_BASE + "TProtocolFieldGetter";
}

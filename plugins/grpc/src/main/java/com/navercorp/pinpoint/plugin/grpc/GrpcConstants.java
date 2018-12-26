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

package com.navercorp.pinpoint.plugin.grpc;

import com.navercorp.pinpoint.common.trace.AnnotationKey;
import com.navercorp.pinpoint.common.trace.AnnotationKeyFactory;
import com.navercorp.pinpoint.common.trace.ServiceType;
import com.navercorp.pinpoint.common.trace.ServiceTypeFactory;

import static com.navercorp.pinpoint.common.trace.AnnotationKeyProperty.VIEW_IN_RECORD_SET;
import static com.navercorp.pinpoint.common.trace.ServiceTypeProperty.RECORD_STATISTICS;

public final class GrpcConstants {

    private GrpcConstants() {
    }

    public static final ServiceType SERVICE_TYPE = ServiceTypeFactory.of(9160, "gRPC", RECORD_STATISTICS);
    public static final ServiceType SERVICE_TYPE_INTERNAL = ServiceTypeFactory.of(9161, "gRPC_INTERNAL");

    public static final ServiceType SERVER_SERVICE_TYPE = ServiceTypeFactory.of(1130, "gRPC_SERVER", RECORD_STATISTICS);
    public static final ServiceType SERVER_SERVICE_TYPE_INTERNAL = ServiceTypeFactory.of(9162, "gRPC_SERVER_INTERNAL");

    public static final AnnotationKey CLIENT_STATUS_ANNOTATION = AnnotationKeyFactory.of(160, "gRPC.status", VIEW_IN_RECORD_SET);

    public static final String UNKNOWN_ADDRESS = "Unknown";
    public static final String UNKNOWN_METHOD = "UnknownMethod";

}

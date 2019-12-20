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
import com.navercorp.pinpoint.common.trace.AnnotationKeyProvider;
import com.navercorp.pinpoint.common.trace.ServiceType;

import com.navercorp.pinpoint.common.trace.ServiceTypeProvider;

public final class GrpcConstants {

    private GrpcConstants() {
    }

    public static final ServiceType SERVICE_TYPE = ServiceTypeProvider.getByName("GRPC");
    public static final ServiceType SERVICE_TYPE_INTERNAL = ServiceTypeProvider.getByName("GRPC_INTERNAL");

    public static final ServiceType SERVER_SERVICE_TYPE = ServiceTypeProvider.getByName("GRPC_SERVER");
    public static final ServiceType SERVER_SERVICE_TYPE_INTERNAL = ServiceTypeProvider.getByName("GRPC_SERVER_INTERNAL");

    public static final AnnotationKey CLIENT_STATUS_ANNOTATION = AnnotationKeyProvider.getByCode(160);

    public static final String UNKNOWN_ADDRESS = "Unknown";
    public static final String UNKNOWN_METHOD = "UnknownMethod";

}

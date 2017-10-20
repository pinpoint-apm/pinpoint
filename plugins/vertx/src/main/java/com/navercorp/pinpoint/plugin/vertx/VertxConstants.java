/*
 * Copyright 2016 NAVER Corp.
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
package com.navercorp.pinpoint.plugin.vertx;

import com.navercorp.pinpoint.common.trace.ServiceType;
import com.navercorp.pinpoint.common.trace.ServiceTypeFactory;
import com.navercorp.pinpoint.common.trace.ServiceTypeProperty;

/**
 * @author jaehong.kim
 */
public class VertxConstants {
    public static final ServiceType VERTX = ServiceTypeFactory.of(1050, "VERTX", ServiceTypeProperty.RECORD_STATISTICS);
    public static final ServiceType VERTX_INTERNAL = ServiceTypeFactory.of(1051, "VERTX_INTERNAL", "VERTX");
    public static final ServiceType VERTX_HTTP_SERVER = ServiceTypeFactory.of(1052, "VERTX_HTTP_SERVER", ServiceTypeProperty.RECORD_STATISTICS);
    public static final ServiceType VERTX_HTTP_SERVER_INTERNAL = ServiceTypeFactory.of(1053, "VERTX_HTTP_SERVER_INTERNAL", "VERTX_HTTP_SERVER");
    public static final ServiceType VERTX_HTTP_CLIENT = ServiceTypeFactory.of(9130, "VERTX_HTTP_CLIENT", ServiceTypeProperty.RECORD_STATISTICS);
    public static final ServiceType VERTX_HTTP_CLIENT_INTERNAL = ServiceTypeFactory.of(9131, "VERTX_HTTP_CLIENT_INTERNAL", "VERTX_HTTP_CLIENT");

    public static final String HTTP_CLIENT_REQUEST_SCOPE = "HttpClientRequestScope";
    public static final String HTTP_CLIENT_CREATE_REQUEST_SCOPE = "HttpClientCreateRequestScope";
}

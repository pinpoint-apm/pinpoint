/*
 * Copyright 2018 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.navercorp.pinpoint.plugin.vertx;

import com.navercorp.pinpoint.common.trace.ServiceType;
import com.navercorp.pinpoint.common.trace.ServiceTypeProvider;

/**
 * @author jaehong.kim
 */
public final class VertxConstants {
    private VertxConstants() {
    }

    public static final ServiceType VERTX = ServiceTypeProvider.getByName("VERTX");
    public static final ServiceType VERTX_INTERNAL = ServiceTypeProvider.getByName("VERTX_INTERNAL");
    public static final ServiceType VERTX_HTTP_SERVER = ServiceTypeProvider.getByName("VERTX_HTTP_SERVER");
    public static final ServiceType VERTX_HTTP_SERVER_INTERNAL = ServiceTypeProvider.getByName("VERTX_HTTP_SERVER_INTERNAL");
    public static final ServiceType VERTX_HTTP_CLIENT = ServiceTypeProvider.getByName("VERTX_HTTP_CLIENT");
    public static final ServiceType VERTX_HTTP_CLIENT_INTERNAL = ServiceTypeProvider.getByName("VERTX_HTTP_CLIENT_INTERNAL");

    public static final String HTTP_CLIENT_REQUEST_SCOPE = "HttpClientRequestScope";
    public static final String HTTP_CLIENT_CREATE_REQUEST_SCOPE = "HttpClientCreateRequestScope";

    public static final String[] VERTX_URI_MAPPING_CONTEXT_KEYS = {"pinpoint.metric.uri-template"};
}

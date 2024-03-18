/*
 * Copyright 2014 NAVER Corp.
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
package com.navercorp.pinpoint.plugin.okhttp;

import com.navercorp.pinpoint.common.trace.ServiceType;
import com.navercorp.pinpoint.common.trace.ServiceTypeFactory;

import static com.navercorp.pinpoint.common.trace.ServiceTypeProperty.RECORD_STATISTICS;


/**
 * 
 * @author jaehong.kim
 *
 */
public final class OkHttpConstants {
    private OkHttpConstants() {
    }

    public static final ServiceType OK_HTTP_CLIENT = ServiceTypeFactory.of(9058, "OK_HTTP_CLIENT", RECORD_STATISTICS);
    public static final ServiceType OK_HTTP_CLIENT_INTERNAL = ServiceTypeFactory.of(9059, "OK_HTTP_CLIENT_INTERNAL", "OK_HTTP_CLIENT");

    public static final String SEND_REQUEST_SCOPE = "SendRequestScope";
    public static final String CALL_SCOPE = "CallScope";

    public static final String FIELD_USER_REQUEST = "userRequest";
    public static final String FIELD_USER_RESPONSE = "userResponse";
    public static final String FIELD_CONNECTION = "connection";
    public static final String FIELD_HTTP_URL = "url";

    public static final Class<?> CONNECTION_GETTER_V2 = com.navercorp.pinpoint.plugin.okhttp.v2.ConnectionGetter.class;
    public static final Class<?> HTTP_URL_GETTER = com.navercorp.pinpoint.plugin.okhttp.v2.HttpUrlGetter.class;
    public static final Class<?> URL_GETTER = com.navercorp.pinpoint.plugin.okhttp.v2.UrlGetter.class;
    public static final Class<?> USER_REQUEST_GETTER_V2 = com.navercorp.pinpoint.plugin.okhttp.v2.UserRequestGetter.class;
    public static final Class<?> USER_REQUEST_GETTER_V3 = com.navercorp.pinpoint.plugin.okhttp.v3.UserRequestGetter.class;
    public static final Class<?> USER_RESPONSE_GETTER_V2 = com.navercorp.pinpoint.plugin.okhttp.v2.UserResponseGetter.class;
    public static final Class<?> USER_RESPONSE_GETTER_V3 = com.navercorp.pinpoint.plugin.okhttp.v3.UserResponseGetter.class;
}

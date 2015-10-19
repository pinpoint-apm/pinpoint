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
package com.navercorp.pinpoint.plugin.httpclient3;

import static com.navercorp.pinpoint.common.trace.ServiceTypeProperty.*;

import com.navercorp.pinpoint.common.trace.ServiceType;
import com.navercorp.pinpoint.common.trace.ServiceTypeFactory;

/**
 * 
 * @author jaehong.kim
 *
 */
public final class HttpClient3Constants {
    private HttpClient3Constants() {
    }

    public static final ServiceType HTTP_CLIENT_3 = ServiceTypeFactory.of(9050, "HTTP_CLIENT_3", RECORD_STATISTICS);
    public static final ServiceType HTTP_CLIENT_3_INTERNAL = ServiceTypeFactory.of(9051, "HTTP_CLIENT_3_INTERNAL", "HTTP_CLIENT_3");

    public static final String HTTP_CLIENT3_SCOPE = "HttpClient3Scope"; 
    public static final String HTTP_CLIENT3_CONNECTION_SCOPE = "HttpClient3HttpConnection";
    public static final String HTTP_CLIENT3_METHOD_BASE_SCOPE = "HttpClient3MethodBase";
    public static final String FIELD_HOST_NAME = "hostName";
    public static final String FIELD_PORT_NUMBER = "portNumber";
    public static final String FIELD_PROXY_HOST_NAME = "proxyHostName";
    public static final String FIELD_PROXY_PORT_NUMBER = "proxyPortNumber";
}
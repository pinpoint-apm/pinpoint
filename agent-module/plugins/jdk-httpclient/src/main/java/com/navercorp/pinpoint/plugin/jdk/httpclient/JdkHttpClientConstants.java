/*
 * Copyright 2024 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.navercorp.pinpoint.plugin.jdk.httpclient;

import com.navercorp.pinpoint.common.trace.ServiceType;
import com.navercorp.pinpoint.common.trace.ServiceTypeProvider;

public class JdkHttpClientConstants {
    private JdkHttpClientConstants() {
    }

    public static final ServiceType JDK_HTTP_CLIENT = ServiceTypeProvider.getByName("JDK_HTTP_CLIENT");
    public static final ServiceType JDK_HTTP_CLIENT_INTERNAL = ServiceTypeProvider.getByName("JDK_HTTP_CLIENT_INTERNAL");
    public static final String HTTP_CLIENT_SEND_SCOPE = "HttpClientSendScope";
}

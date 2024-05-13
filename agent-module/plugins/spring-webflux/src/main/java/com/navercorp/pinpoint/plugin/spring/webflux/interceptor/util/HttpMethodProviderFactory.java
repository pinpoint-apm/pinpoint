/*
 * Copyright 2024 NAVER Corp.
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
package com.navercorp.pinpoint.plugin.spring.webflux.interceptor.util;

import com.navercorp.pinpoint.plugin.spring.webflux.SpringVersion;

public class HttpMethodProviderFactory {

    public HttpMethodProviderFactory() {
    }

    public static HttpMethodProvider getHttpMethodProvider(int version) {
        switch (version) {
            case SpringVersion.SPRING_VERSION_5:
                return new Spring5HttpMethodProvider();
            case SpringVersion.SPRING_VERSION_6:
                return new Spring6HttpMethodProvider();
            default:
                return new UnsupportedHttpMethodProvider();
        }
    }
}

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

import org.springframework.http.client.reactive.ClientHttpResponse;

/**
 * @author intr3p1d
 */
public class Spring6HttpStatusProvider implements HttpStatusProvider {
    @Override
    public int getStatusCode(Object target) {
        if (target instanceof ClientHttpResponse) {
            final ClientHttpResponse response = (ClientHttpResponse) target;
            try {
                return response.getStatusCode().value();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        return -1;
    }
}

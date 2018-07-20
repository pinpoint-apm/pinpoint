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

package com.navercorp.pinpoint.plugin.ning.asynchttpclient;

import com.navercorp.pinpoint.common.trace.ServiceType;
import com.navercorp.pinpoint.common.trace.ServiceTypeFactory;

import static com.navercorp.pinpoint.common.trace.ServiceTypeProperty.RECORD_STATISTICS;

/**
 * @author Woonduk Kang(emeroad)
 */
public final class NingAsyncHttpClientConstants {
    private NingAsyncHttpClientConstants() {
    }

    public static final ServiceType ASYNC_HTTP_CLIENT = ServiceTypeFactory.of(9056, "ASYNC_HTTP_CLIENT", RECORD_STATISTICS);
    public static final ServiceType ASYNC_HTTP_CLIENT_INTERNAL = ServiceTypeFactory.of(9057, "ASYNC_HTTP_CLIENT_INTERNAL", "ASYNC_HTTP_CLIENT");
}

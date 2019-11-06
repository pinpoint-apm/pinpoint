/*
 * Copyright 2017 NAVER Corp.
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

import com.navercorp.pinpoint.bootstrap.context.Header;
import io.vertx.core.http.HttpServerRequest;

/**
 * @author jaehong.kim
 */
public class VertxHttpHeaderFilter {
    private final boolean enable;

    public VertxHttpHeaderFilter(boolean enable) {
        this.enable = enable;
    }

    public void filter(final HttpServerRequest request) {
        if (!enable || request == null || request.headers() == null) {
            return;
        }

        for (String name : request.headers().names()) {
            if (Header.startWithPinpointHeader(name)) {
                request.headers().remove(name);
            }
        }
    }
}

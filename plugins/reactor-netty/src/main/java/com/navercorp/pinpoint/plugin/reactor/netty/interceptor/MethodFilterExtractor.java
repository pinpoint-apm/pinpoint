/*
 * Copyright 2019 NAVER Corp.
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

package com.navercorp.pinpoint.plugin.reactor.netty.interceptor;

import com.navercorp.pinpoint.bootstrap.config.Filter;
import com.navercorp.pinpoint.bootstrap.plugin.request.util.ParameterExtractor;
import java.util.Objects;
import reactor.netty.http.server.HttpServerRequest;

/**
 * @author Woonduk Kang(emeroad)
 */
public class MethodFilterExtractor implements ParameterExtractor<HttpServerRequest> {
    private final Filter<String> excludeProfileMethodFilter;
    private final ParameterExtractor<HttpServerRequest> delegate;

    public MethodFilterExtractor(Filter<String> excludeProfileMethodFilter, ParameterExtractor<HttpServerRequest> delegate) {
        this.excludeProfileMethodFilter = Objects.requireNonNull(excludeProfileMethodFilter, "excludeProfileMethodFilter must not be null");
        this.delegate = Objects.requireNonNull(delegate, "delegate must not be null");
    }

    @Override
    public String extractParameter(HttpServerRequest httpServletRequest) {
        if (excludeProfileMethodFilter.filter(httpServletRequest.method().toString())) {
            return null;
        }
        return delegate.extractParameter(httpServletRequest);
    }
}
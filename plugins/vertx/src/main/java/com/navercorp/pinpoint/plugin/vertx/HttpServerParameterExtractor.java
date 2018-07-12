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

import com.navercorp.pinpoint.bootstrap.plugin.request.util.ParameterExtractor;
import com.navercorp.pinpoint.common.util.StringUtils;
import io.vertx.core.http.HttpServerRequest;

import java.util.List;
import java.util.Map;

/**
 * @author Woonduk Kang(emeroad)
 */
public class HttpServerParameterExtractor implements ParameterExtractor<HttpServerRequest> {

    private int eachLimit;
    private int totalLimit;

    public HttpServerParameterExtractor(int eachLimit, int totalLimit) {
        this.eachLimit = eachLimit;
        this.totalLimit = totalLimit;
    }

    @Override
    public String extractParameter(HttpServerRequest request) {
        if (request.params() == null) {
            return "";
        }

        final StringBuilder params = new StringBuilder(64);
        List<Map.Entry<String, String>> entries = request.params().entries();
        for (Map.Entry<String, String> entry : entries) {
            if (params.length() != 0) {
                params.append('&');
            }
            // skip appending parameters if parameter size is bigger than totalLimit
            if (params.length() > totalLimit) {
                params.append("...");
                return params.toString();
            }

            String key = entry.getKey();
            params.append(StringUtils.abbreviate(key, eachLimit));
            params.append('=');
            Object value = entry.getValue();
            if (value != null) {
                params.append(StringUtils.abbreviate(StringUtils.toString(value), eachLimit));
            }
        }
        return params.toString();
    }
}

/*
 *  Copyright 2018 NAVER Corp.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package com.navercorp.pinpoint.plugin.akka.http.resolver;

import akka.http.javadsl.model.HttpHeader;
import akka.http.javadsl.model.HttpRequest;
import com.navercorp.pinpoint.bootstrap.context.RemoteAddressResolver;
import com.navercorp.pinpoint.common.util.StringUtils;
import com.navercorp.pinpoint.plugin.akka.http.AkkaHttpConfig;

import java.util.Optional;

/**
 * @author Taejin Koo
 */
public class HeaderResolver<T extends HttpRequest> implements RemoteAddressResolver<T> {

    // https://doc.akka.io/docs/akka-http/current/routing-dsl/directives/misc-directives/extractClientIP.html

    // X-Forwarded-For, Remote-Address, or X-Real-IP

    private static final String DEFAULT_REMOTE_ADDRESS_HEADER = "Remote-Address";

    private final String remoteAddressHeader;

    public HeaderResolver(AkkaHttpConfig config) {
        String realIpHeader = config.getRealIpHeader();
        if (StringUtils.isEmpty(realIpHeader)) {
            this.remoteAddressHeader = DEFAULT_REMOTE_ADDRESS_HEADER;
        } else {
            this.remoteAddressHeader = realIpHeader;
        }
    }

    @Override
    public String resolve(T target) {
        String value = getHeaderValue(target, remoteAddressHeader, "");
        return value;
    }

    private String getHeaderValue(final HttpRequest request, final String name, String defaultValue) {
        if (request == null) {
            return defaultValue;
        }

        Optional<HttpHeader> optional = request.getHeader(name);
        if (optional == null) {
            return defaultValue;
        }

        HttpHeader header = optional.orElse(null);
        if (header == null) {
            return defaultValue;
        }

        String value = header.value();
        if (value == null) {
            return defaultValue;
        }

        return value;
    }

}

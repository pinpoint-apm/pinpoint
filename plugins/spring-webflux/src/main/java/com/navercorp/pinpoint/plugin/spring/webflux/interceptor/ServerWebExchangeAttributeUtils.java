/*
 * Copyright 2023 NAVER Corp.
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

package com.navercorp.pinpoint.plugin.spring.webflux.interceptor;

import com.navercorp.pinpoint.common.util.StringUtils;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.util.pattern.PathPattern;

public class ServerWebExchangeAttributeUtils {

    public static String extractAttribute(ServerWebExchange webExchange, String[] keys) {
        for (String attributeName : keys) {
            final Object uriMapping = webExchange.getAttribute(attributeName);
            if (!(uriMapping instanceof PathPattern)) {
                continue;
            }

            final String uriTemplate = ((PathPattern) uriMapping).getPatternString();
            if (StringUtils.hasLength(uriTemplate)) {
                return uriTemplate;
            }
        }
        return null;
    }
}

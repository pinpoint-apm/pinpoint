/*
 * Copyright 2018 NAVER Corp.
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

package com.navercorp.pinpoint.plugin.weblogic;

import com.navercorp.pinpoint.bootstrap.plugin.request.util.ParameterExtractor;
import com.navercorp.pinpoint.common.util.StringUtils;
import weblogic.servlet.internal.ServletRequestImpl;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @author jaehong.kim
 */
public class ServletRequestImplParameterExtractor implements ParameterExtractor<ServletRequestImpl> {
    private int eachLimit;
    private int totalLimit;

    public ServletRequestImplParameterExtractor(int eachLimit, int totalLimit) {
        this.eachLimit = eachLimit;
        this.totalLimit = totalLimit;
    }

    @Override
    public String extractParameter(ServletRequestImpl request) {
        String queryString = request.getQueryString();
        final StringBuilder params = new StringBuilder(64);
        try {
            final Map<String, String> queryPairs = splitQuery(queryString);

            for (Map.Entry<String, String> attrs : queryPairs.entrySet()) {

                if (params.length() != 0) {
                    params.append('&');
                }
                // skip appending parameters if parameter size is bigger than
                // totalLimit
                if (params.length() > totalLimit) {
                    params.append("...");
                    return params.toString();
                }

                final String key = attrs.getKey();
                params.append(StringUtils.abbreviate(key, eachLimit));
                params.append('=');
                final String value = attrs.getValue();
                if (value != null) {
                    params.append(StringUtils.abbreviate(StringUtils.toString(value), eachLimit));
                }
            }
        } catch (UnsupportedEncodingException ignore) {
            // ignore
        }
        return params.toString();
    }

    private Map<String, String> splitQuery(String query) throws UnsupportedEncodingException {
        Map<String, String> query_pairs = new LinkedHashMap<String, String>();
        if (query != null) {
            String[] pairs = query.split("&");
            for (String pair : pairs) {
                int idx = pair.indexOf('=');
                if (idx > 0)
                    query_pairs.put(decode(pair.substring(0, idx)), decode(pair.substring(idx + 1)));
            }
        }
        return query_pairs;
    }

    private String decode(String substring) throws UnsupportedEncodingException {
        return URLDecoder.decode(substring, "UTF-8");
    }
}
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

package com.navercorp.pinpoint.plugin.websphere;

import com.ibm.websphere.servlet.request.IRequest;
import com.navercorp.pinpoint.bootstrap.plugin.request.util.ParameterExtractor;
import com.navercorp.pinpoint.common.util.StringUtils;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @author jaehong.kim
 */
public class IRequestParameterExtractor implements ParameterExtractor<IRequest> {

    private int eachLimit;
    private int totalLimit;

    public IRequestParameterExtractor(int eachLimit, int totalLimit) {
        this.eachLimit = eachLimit;
        this.totalLimit = totalLimit;
    }

    @Override
    public String extractParameter(IRequest request) {
        String queryString = request.getQueryString();
        final StringBuilder params = new StringBuilder(64);
        try {
            Map<String, String> query_pairs = splitQuery(queryString);
            for (Map.Entry<String, String> entry : query_pairs.entrySet()) {
                if (params.length() != 0) {
                    params.append('&');
                }
                // skip appending parameters if parameter size is bigger than
                // totalLimit
                if (params.length() > totalLimit) {
                    params.append("...");
                    return params.toString();
                }
                String key = entry.getKey();
                params.append(StringUtils.abbreviate(key, eachLimit));
                params.append('=');
                String value = entry.getValue();
                if (value != null) {
                    params.append(StringUtils.abbreviate(StringUtils.toString(value), eachLimit));
                }
            }
        } catch (UnsupportedEncodingException e) {
        }
        return params.toString();
    }

    private Map<String, String> splitQuery(String query) throws UnsupportedEncodingException {
        Map<String, String> query_pairs = new LinkedHashMap<String, String>();
        if (query != null) {
            String[] pairs = query.split("&");
            for (String pair : pairs) {
                int idx = pair.indexOf('=');
                if (idx > 0) {
                    query_pairs.put(URLDecoder.decode(pair.substring(0, idx), "UTF-8"), URLDecoder.decode(pair.substring(idx + 1), "UTF-8"));
                }
            }
        }
        return query_pairs;
    }
}

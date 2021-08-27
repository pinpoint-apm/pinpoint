/*
 * Copyright 2021 NAVER Corp.
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

package com.navercorp.pinpoint.plugin.httpclient3;

import com.navercorp.pinpoint.bootstrap.plugin.response.ResponseAdaptor;
import com.navercorp.pinpoint.common.util.ArrayUtils;
import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpMethod;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author yjqg6666
 */
public class HttpClient3ResponseHeaderAdaptor implements ResponseAdaptor<HttpMethod> {

    @Override
    public boolean containsHeader(HttpMethod response, String name) {
        return response.getResponseHeader(name) != null;
    }

    @Override
    public void setHeader(HttpMethod response, String name, String value) {

    }

    @Override
    public void addHeader(HttpMethod response, String name, String value) {

    }

    @Override
    public String getHeader(HttpMethod response, String name) {
        final Header header = response.getResponseHeader(name);
        return header != null ? header.getValue() : null;
    }

    @Override
    public Collection<String> getHeaders(HttpMethod response, String name) {
        final Header[] headers = response.getResponseHeaders(name);
        if (ArrayUtils.isEmpty(headers)) {
            return Collections.emptyList();
        }
        if (headers.length == 1) {
            return Collections.singletonList(headers[0].getValue());
        }
        Set<String> values = new HashSet<>(headers.length);
        for (Header header : headers) {
            values.add(header.getValue());
        }
        return values;
    }

    @Override
    public Collection<String> getHeaderNames(HttpMethod response) {
        final Header[] headers = response.getResponseHeaders();
        if (ArrayUtils.isEmpty(headers)) {
            return Collections.emptyList();
        }
        if (headers.length == 1) {
            return Collections.singletonList(headers[0].getName());
        }
        Set<String> values = new HashSet<>(headers.length);
        for (Header header : headers) {
            values.add(header.getName());
        }
        return values;
    }
}

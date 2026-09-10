/*
 * Copyright 2021 NAVER Corp.
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

package com.navercorp.pinpoint.plugin.spring.webflux;

import com.navercorp.pinpoint.bootstrap.plugin.response.ResponseAdaptor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.client.reactive.ClientHttpResponse;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Only {@link HttpHeaders} methods whose signature is identical in Spring 5, 6 and 7 are used
 * ({@code getFirst}, {@code set}, {@code add}, {@code forEach}). Spring Framework 7.0 dropped the
 * {@code MultiValueMap} implementation from {@code HttpHeaders}: {@code containsKey(Object)} and
 * {@code keySet()} are gone and {@code get(Object)} became {@code get(String)}, so the {@code Map}
 * calls this class was compiled against on 5.3 fail with {@code NoSuchMethodError} at runtime.
 * {@code forEach(BiConsumer)} is declared on {@code HttpHeaders} in 6.x/7.x and inherited from
 * {@code Map} as a default method in 5.x, so it resolves on every version.
 */
public class SpringWebFluxResponseHeaderAdaptor implements ResponseAdaptor<ClientHttpResponse> {

    @Override
    public boolean containsHeader(ClientHttpResponse response, String name) {
        return response.getHeaders().getFirst(name) != null;
    }

    @Override
    public void setHeader(ClientHttpResponse response, String name, String value) {
        response.getHeaders().set(name, value);
    }

    @Override
    public void addHeader(ClientHttpResponse response, String name, String value) {
        response.getHeaders().add(name, value);
    }

    @Override
    public String getHeader(ClientHttpResponse response, String name) {
        return response.getHeaders().getFirst(name);
    }

    @Override
    public Collection<String> getHeaders(ClientHttpResponse response, String name) {
        final List<String> values = new ArrayList<>();
        // HTTP header names are case-insensitive, as is HttpHeaders' own lookup.
        response.getHeaders().forEach((key, list) -> {
            if (name.equalsIgnoreCase(key) && list != null) {
                values.addAll(list);
            }
        });
        return values;
    }

    @Override
    public Collection<String> getHeaderNames(ClientHttpResponse response) {
        final List<String> names = new ArrayList<>();
        response.getHeaders().forEach((key, list) -> names.add(key));
        return names;
    }
}

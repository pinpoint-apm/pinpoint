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

package com.pinpointest.plugin.controller;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;

@RestController
public class HttpHeaderController {

    private final Logger logger = LogManager.getLogger(this.getClass());

    @GetMapping(value = "/reqHeader")
    public String reqHeader(HttpServletRequest request) {
        Collection<String> headerNames = Collections.list(request.getHeaderNames());

        Function<String, Collection<String>> getHeaders = (name) -> Collections.list(request.getHeaders(name));

        List<Pair<String, Collection<String>>> allHeader = getAllHeader(getHeaders, headerNames);

        for (Pair<String, Collection<String>> pair : allHeader) {
            logger.info("req {}:{}", pair.getKey(), pair.getValue());
        }

        return format(allHeader);
    }

    private <K, V> List<Pair<K, V>> getAllHeader(Function<K, V> headerFunction, Collection<K> headerNames) {
        List<Pair<K, V>> result = new ArrayList<>();

        for (K headerName : headerNames) {
            V headers = headerFunction.apply(headerName);
            result.add(new Pair<>(headerName, headers));
        }

        return result;
    }

    @GetMapping(value = "/resHeader")
    public String resHeader(HttpServletResponse response) {
        response.addHeader("TEST_HEADER", "1");
        response.addHeader("TEST_HEADER", "2");
        response.addHeader("TEST_HEADER", "3");
        response.addHeader("TEST_HEADER", "a, b, c");

        Collection<String> headerNames = response.getHeaderNames();
        for (String headerName : headerNames) {
            String header = response.getHeader(headerName);
            logger.info("res getHeader: {}:{}",headerName, header);
        }


        Function<String, Collection<String>> getHeaders = response::getHeaders;

        List<Pair<String, Collection<String>>> allHeader = getAllHeader(getHeaders, headerNames);

        logger.info("response header dump : API");
        for (Pair<String, Collection<String>> pair : allHeader) {
            logger.info("res {}:{}", pair.getKey(), pair.getValue());
        }

        return format(allHeader);
    }

    private String format(List<Pair<String, Collection<String>>> allHeader) {
        StringBuilder sb = new StringBuilder();
        for (Pair<String, Collection<String>> pair : allHeader) {
            sb.append(pair.getKey());
            sb.append(":");

            sb.append(pair.getValue());
            sb.append("\n");
        }

        return sb.toString();
    }
}

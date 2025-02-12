/*
 * Copyright 2025 NAVER Corp.
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
package com.navercorp.pinpoint.web;

import com.navercorp.pinpoint.web.problem.ProblemWrapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.context.request.NativeWebRequest;
import org.zalando.problem.Problem;
import org.zalando.problem.spring.web.advice.ProblemHandling;

import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * @author intr3p1d
 */
@ControllerAdvice
final class ExceptionHandling implements ProblemHandling {

    private final String hostname;

    ExceptionHandling() {
        this.hostname = getHostName();
    }

    static private String getHostName() {
        try {
            return InetAddress.getLocalHost().getHostName();
        } catch (UnknownHostException e) {
            return "unknown";
        }
    }

    @Override
    public ResponseEntity<Problem> process(ResponseEntity<Problem> entity, NativeWebRequest request) {
        Problem originalProblem = entity.getBody();
        if (originalProblem == null) {
            return entity;
        }

        HttpServletRequest httpRequest = request.getNativeRequest(HttpServletRequest.class);
        HttpServletResponse httpResponse = request.getNativeResponse(HttpServletResponse.class);

        String path = (httpRequest != null) ? httpRequest.getRequestURI() : "unknown";
        String method = (httpRequest != null) ? httpRequest.getMethod() : "unknown";
        int statusCode = (httpResponse != null) ? httpResponse.getStatus() : entity.getStatusCode().value();

        Problem modifiedProblem = new ProblemWrapper(originalProblem, hostname, path, method);
        return ResponseEntity.status(statusCode).body(modifiedProblem);
    }

    @Override
    public boolean isCausalChainsEnabled() {
        return true;
    }

}

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

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.Nullable;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Collection;

import static com.navercorp.pinpoint.web.problem.StackTraceProcessor.COMPOUND;
import static java.util.Arrays.asList;

/**
 * @author intr3p1d
 */
@ControllerAdvice
final class CustomExceptionHandler extends ResponseEntityExceptionHandler {

    private static final ToStringSerializer serializer = new ToStringSerializer();
    private final String hostname;

    CustomExceptionHandler() {
        this.hostname = getHostName();
    }

    static private String getHostName() {
        try {
            return InetAddress.getLocalHost().getHostName();
        } catch (UnknownHostException e) {
            return "unknown";
        }
    }

    @ExceptionHandler({Exception.class})
    public ResponseEntity<ProblemDetail> handleGeneralException(
            Exception ex,
            WebRequest request
    ) {
        HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
        ProblemDetail problemDetail = ProblemDetail.forStatus(status.value());
        problemDetail.setTitle("Internal Server Error");
        problemDetail.setDetail(ex.getMessage());
        addProperties(problemDetail, request);
        addStackTraces(problemDetail, ex);

        return new ResponseEntity<>(problemDetail, status);
    }

    @Override
    protected ResponseEntity<Object> handleExceptionInternal(
            Exception ex,
            @Nullable Object body,
            HttpHeaders headers,
            HttpStatusCode statusCode,
            WebRequest request
    ) {
        ResponseEntity<Object> response = super.handleExceptionInternal(ex, body, headers, statusCode, request);

        if (response.getBody() instanceof ProblemDetail problemDetail) {
            addProperties(problemDetail, request);
            addStackTraces(problemDetail, ex);
            return this.createResponseEntity(problemDetail, headers, statusCode, request);
        }
        return response;
    }

    public void addProperties(ProblemDetail problemDetail, WebRequest request) {
        if (request instanceof ServletWebRequest servletWebRequest) {
            problemDetail.setProperty("method", servletWebRequest.getRequest().getMethod());
        } else {
            problemDetail.setProperty("method", "UNKNOWN");
        }
        problemDetail.setProperty("parameters", request.getParameterMap());
        problemDetail.setProperty("hostname", hostname);
    }

    public void addStackTraces(ProblemDetail problemDetail, Throwable th) {
        final Collection<StackTraceElement> stackTrace = COMPOUND.process(asList(th.getStackTrace()));
        String[] trace = traceToStringArray(stackTrace);
        problemDetail.setProperty("trace", trace);
    }

    private String[] traceToStringArray(Collection<StackTraceElement> stackTrace) {
        return stackTrace.stream().map(serializer::valueToString).toArray(String[]::new);
    }
}

/*
 * Copyright 2025 NAVER Corp.
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
package com.navercorp.pinpoint.web;

import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.ErrorResponse;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.util.Collection;
import java.util.Objects;

import static com.navercorp.pinpoint.web.problem.StackTraceProcessor.COMPOUND;
import static java.util.Arrays.asList;

/**
 * @author intr3p1d
 */
@ControllerAdvice
public final class CustomExceptionHandler extends ResponseEntityExceptionHandler {

    private static final ToStringSerializer serializer = ToStringSerializer.instance;

    private final String hostname;
    private final Logger logger = LogManager.getLogger(this.getClass());

    public CustomExceptionHandler(String hostname) {
        this.hostname = Objects.requireNonNull(hostname, "hostname");
    }

    @ExceptionHandler({Throwable.class})
    public ResponseEntity<ProblemDetail> handleGeneralException(
            Throwable ex,
            WebRequest request
    ) {
        logger.warn("handleGeneralException: {}", ex.getMessage(), ex);

        if (ex instanceof ErrorResponse errorResponse) {
            ProblemDetail problemDetail = errorResponse.getBody();
            addProperties(problemDetail, request);
            addStackTraces(problemDetail, ex);
            return new ResponseEntity<>(problemDetail, errorResponse.getHeaders(), errorResponse.getStatusCode());
        }
        HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
        ProblemDetail problemDetail = ProblemDetail.forStatus(status);
        problemDetail.setTitle("Internal Server Error");
        problemDetail.setDetail(ex.getMessage());
        addProperties(problemDetail, request);
        addStackTraces(problemDetail, ex);

        return new ResponseEntity<>(problemDetail, status);
    }

    @Override
    protected ResponseEntity<Object> handleExceptionInternal(
            @NonNull Exception ex,
            @Nullable Object body,
            @NonNull HttpHeaders headers,
            @NonNull HttpStatusCode statusCode,
            @NonNull WebRequest request
    ) {
        logger.warn("handleExceptionInternal: {}", ex, ex);

        ResponseEntity<Object> response = super.handleExceptionInternal(ex, body, headers, statusCode, request);
        if (response == null) {
            return ResponseEntity.status(statusCode).build();
        }
        Object responseBody = response.getBody();
        if (responseBody instanceof ProblemDetail problemDetail) {
            addProperties(problemDetail, request);
            addStackTraces(problemDetail, ex);
            return this.createResponseEntity(problemDetail, headers, statusCode, request);
        }
        return response;
    }

    public void addProperties(ProblemDetail problemDetail, WebRequest request) {
        problemDetail.setProperty("method", getMethod(request));
        problemDetail.setProperty("parameters", request.getParameterMap());
        problemDetail.setProperty("hostname", hostname);
    }

    private String getMethod(WebRequest request) {
        if (request instanceof ServletWebRequest servletWebRequest) {
            return servletWebRequest.getRequest().getMethod();
        } else {
            return "UNKNOWN";
        }
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

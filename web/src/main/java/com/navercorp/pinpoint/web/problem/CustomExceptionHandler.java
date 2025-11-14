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
package com.navercorp.pinpoint.web.problem;

import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.springframework.boot.autoconfigure.web.ErrorProperties;
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

    private final Logger logger = LogManager.getLogger(this.getClass());

    private static final ToStringSerializer serializer = ToStringSerializer.instance;

    private static final String[] EXCLUDE_PATHS = {
            "/favicon.ico",
            "/.well-known"
    };

    private final String hostname;
    private final ErrorProperties errorProperties;



    public CustomExceptionHandler(String hostname, ErrorProperties errorProperties) {
        this.hostname = Objects.requireNonNull(hostname, "hostname");
        this.errorProperties = Objects.requireNonNull(errorProperties, "errorProperties");
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
        problemDetail.setTitle(status.getReasonPhrase());
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
        ResponseEntity<Object> response = super.handleExceptionInternal(ex, body, headers, statusCode, request);
        if (response == null) {
            nullResponseLog(ex, request);
            return null;
        }
        if (allowErrorReport(request)) {
            logger.info("handleExceptionInternal  {}", ex, ex);
        }
        Object responseBody = response.getBody();
        if (responseBody instanceof ProblemDetail problemDetail) {
            addProperties(problemDetail, request);
            addStackTraces(problemDetail, ex);
        }
        return response;
    }

    private void nullResponseLog(@NotNull Exception ex, @NotNull WebRequest request) {
        if (request instanceof ServletWebRequest servletWebRequest) {
            HttpServletResponse response = servletWebRequest.getResponse();
            if (response != null && response.isCommitted()) {
                    logger.info("Response already committed: StackTrace {}", ex, ex);
            }
        }
    }

    private boolean allowErrorReport(WebRequest webRequest) {
        String path = getPath(webRequest);
        for (String excludePath : EXCLUDE_PATHS) {
            if (path.startsWith(excludePath)) {
                return false;
            }
        }
        return true;
    }

    public void addProperties(ProblemDetail problemDetail, WebRequest request) {
        problemDetail.setProperty("method", getMethod(request));
        problemDetail.setProperty("parameters", request.getParameterMap());
        problemDetail.setProperty("hostname", hostname);
    }

    private String getMethod(WebRequest webRequest) {
        if (webRequest instanceof ServletWebRequest servletWebRequest) {
            return servletWebRequest.getRequest().getMethod();
        } else {
            return "UNKNOWN";
        }
    }

    public String getPath(WebRequest webRequest) {
        if (webRequest instanceof ServletWebRequest servletWebRequest) {
            HttpServletRequest request = servletWebRequest.getRequest();
            return request.getRequestURI();
        }
        return "/UNKNOWN";
    }

    public void addStackTraces(ProblemDetail problemDetail, Throwable th) {
        if (errorProperties.getIncludeStacktrace() == ErrorProperties.IncludeAttribute.ALWAYS) {
            final Collection<StackTraceElement> stackTrace = COMPOUND.process(asList(th.getStackTrace()));
            String[] trace = traceToStringArray(stackTrace);
            problemDetail.setProperty("trace", trace);
        }
    }

    private String[] traceToStringArray(Collection<StackTraceElement> stackTrace) {
        return stackTrace.stream().map(serializer::valueToString).toArray(String[]::new);
    }
}

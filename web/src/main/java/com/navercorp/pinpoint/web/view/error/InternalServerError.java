package com.navercorp.pinpoint.web.view.error;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class InternalServerError {
    private final String message;
    private final String stackTrace;
    private final RequestInfo requestInfo;

    public InternalServerError(String message, String stackTrace, RequestInfo requestInfo) {
        this.message = message;
        this.requestInfo = requestInfo;
        this.stackTrace = stackTrace;
    }

    @JsonProperty("message")
    public String getMessage() {
        return message;
    }

    @JsonProperty("request")
    public RequestInfo getRequestInfo() {
        return requestInfo;
    }

    @JsonProperty("stacktrace")
    public String getStackTrace() {
        return stackTrace;
    }

    @Override
    public String toString() {
        return "InternalError{" +
                "message='" + message + '\'' +
                ", stackTrace='" + stackTrace + '\'' +
                ", requestInfo=" + requestInfo +
                '}';
    }

    public static class RequestInfo {
        private final String method;
        private final String url;
        private final Map<String, List<String>> headers;
        private final Map<String, List<String>> parameters;

        public RequestInfo(String method, String url, Map<String, List<String>> headers, Map<String, List<String>> parameters) {
            this.method = method;
            this.url = url;
            this.headers = headers;
            this.parameters = parameters;
        }

        public String getMethod() {
            return method;
        }

        public String getUrl() {
            return url;
        }

        @JsonProperty("heads")
        public Map<String, List<String>> getHeaders() {
            return headers;
        }

        public Map<String, List<String>> getParameters() {
            return parameters;
        }

        @Override
        public String toString() {
            return "RequestInfo{" +
                    "method='" + method + '\'' +
                    ", url='" + url + '\'' +
                    ", headers=" + headers +
                    ", parameters=" + parameters +
                    '}';
        }
    }
}

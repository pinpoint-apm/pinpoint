package com.navercorp.pinpoint.web.view.error;

import com.fasterxml.jackson.annotation.JsonInclude;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.Enumeration;
import java.util.Collections;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class PinpointErrorData {
    private final RequestInfo requestInfo;

    public PinpointErrorData(HttpServletRequest request) {
        this.requestInfo = new RequestInfo(request);
    }

    public RequestInfo getRequestInfo() {
        return requestInfo;
    }

    public static class RequestInfo {
        private static final String UNKNOWN = "UNKNOWN";
        private final String method;
        private final String url;
        private final Map<String, List<String>> headers;
        private final Map<String, String[]> parameters;

        public RequestInfo(HttpServletRequest request) {
            this.method = request.getMethod();
            this.url = getRequestUrl(request);
            this.headers = getRequestHeaders(request);
            this.parameters = request.getParameterMap();;
        }

        public String getMethod() {
            return method;
        }

        public String getUrl() {
            return url;
        }

        public Map<String, List<String>> getHeaders() {
            return headers;
        }

        public Map<String, String[]> getParameters() {
            return parameters;
        }

        private String getRequestUrl(HttpServletRequest request) {
            if (request.getRequestURL() == null) {
                return UNKNOWN;
            }
            return request.getRequestURL().toString();
        }

        private Map<String, List<String>> getRequestHeaders(HttpServletRequest request) {
            Enumeration<String> keys = request.getHeaderNames();
            if (keys == null) {
                return Collections.emptyMap();
            }

            Map<String, List<String>> result = new HashMap<>();
            while (keys.hasMoreElements()) {
                String key = keys.nextElement();
                if (key == null) {
                    continue;
                }

                result.put(key, getRequestHeaderValueList(request, key));
            }

            return result;
        }

        private List<String> getRequestHeaderValueList(HttpServletRequest request, String key) {
            Enumeration<String> headerValues = request.getHeaders(key);
            if (headerValues == null) {
                return Collections.emptyList();
            }

            List<String> headerValueList = new ArrayList<>();
            while (headerValues.hasMoreElements()) {
                String headerValue = headerValues.nextElement();
                if (headerValue == null) {
                    headerValueList.add("null");
                } else {
                    headerValueList.add(headerValue);
                }
            }

            return headerValueList;
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
package com.navercorp.pinpoint.web.view.error;

import com.fasterxml.jackson.annotation.JsonInclude;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;

import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class PinpointErrorData {
    private final String hostName;
    private final RequestInfo requestInfo;

    public PinpointErrorData(String hostName, WebRequest request) {
        this.hostName = hostName;
        this.requestInfo = new RequestInfo(request);
    }

    public String getHostName() {
        return hostName;
    }

    public RequestInfo getRequestInfo() {
        return requestInfo;
    }

    public static class RequestInfo {
        private static final String UNKNOWN = "UNKNOWN";
        private final String method;
        private final Map<String, List<String>> headers;
        private final Map<String, String[]> parameters;

        public RequestInfo(WebRequest request) {
            if (request instanceof ServletWebRequest webRequest) {
                this.method = webRequest.getRequest().getMethod();
                this.headers = getRequestHeader(webRequest);
                this.parameters = request.getParameterMap();
            } else {
                this.method = "UNKNOWN";
                this.headers = null;
                this.parameters = null;
            }
        }

        public String getMethod() {
            return method;
        }

        public Map<String, List<String>> getHeaders() {
            return headers;
        }

        public Map<String, String[]> getParameters() {
            return parameters;
        }

        private Map<String, List<String>> getRequestHeader(ServletWebRequest webRequest) {
            Iterator<String> keys = webRequest.getHeaderNames();
            if (keys == null) {
                return Collections.emptyMap();
            }

            Map<String, List<String>> result = new HashMap<>();
            while(keys.hasNext()) {
                String key = keys.next();
                if (key == null) {
                    continue;
                }
                result.put(key, List.of(webRequest.getHeaderValues(key)));
            }

            return result;

        }

        @Override
        public String toString() {
            return "RequestInfo{" +
                    "method='" + method + '\'' +
                    ", headers=" + headers +
                    ", parameters=" + parameters +
                    '}';
        }
    }
}
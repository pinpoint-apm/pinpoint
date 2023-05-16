package com.navercorp.pinpoint.web.view.error;

import com.fasterxml.jackson.annotation.JsonInclude;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;

import javax.servlet.http.HttpServletRequest;
import java.util.*;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class PinpointErrorData {
    private final RequestInfo requestInfo;

    public PinpointErrorData(WebRequest request) {
        this.requestInfo = new RequestInfo(request);
    }

    public RequestInfo getRequestInfo() {
        return requestInfo;
    }

    public static class RequestInfo {
        private static final String UNKNOWN = "UNKNOWN";
        private final String method;
        private final String url;
        private  Map<String, List<String>> headers;
        private  Map<String, String[]> parameters;

        public RequestInfo(WebRequest request) {
            if (request instanceof ServletWebRequest) {
                ServletWebRequest webRequest = (ServletWebRequest) request;
                this.method = webRequest.getRequest().getMethod();
                this.url = String.valueOf(webRequest.getRequest().getRequestURL());
                this.headers = getRequestHeader(webRequest);
                this.parameters = request.getParameterMap();
            } else {
                this.method = "UNKNOWN";
                this.url = "UNKNOWN";
                this.headers = null;
                this.parameters = null;
            }
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
                    ", url='" + url + '\'' +
                    ", headers=" + headers +
                    ", parameters=" + parameters +
                    '}';
        }
    }
}
package com.navercorp.pinpoint.web.view.error;

import org.apache.commons.lang3.SystemUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.error.ErrorAttributeOptions;
import org.springframework.boot.web.servlet.error.DefaultErrorAttributes;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.WebRequest;

import java.util.Map;

@Component
public class PinpointErrorAttributes extends DefaultErrorAttributes {
    private final String hostname;

    @Value("${server.error.include-cookies:true}")
    private boolean includeCookies;

    public PinpointErrorAttributes() {
        this.hostname = SystemUtils.getHostName();
    }

    @Override
    public Map<String, Object> getErrorAttributes(WebRequest webRequest, ErrorAttributeOptions options) {
        Map<String, Object> errorAttributes = super.getErrorAttributes(webRequest, options);
        this.removeDuplicateData(errorAttributes);
        this.addCustomData(webRequest, errorAttributes);
        return errorAttributes;
    }

    // removes attributes already present with ProblemDetail
    private void removeDuplicateData(Map<String, Object> errorAttributes) {
        errorAttributes.remove("status");
        errorAttributes.remove("error");        // ProblemDetail already has "title" field
        errorAttributes.remove("path");        // ProblemDetail already has "instance" field
    }

    private void addCustomData(WebRequest webRequest, Map<String, Object> errorAttributes) {
        PinpointErrorData pinpointErrorData = new PinpointErrorData(this.hostname, webRequest, includeCookies);
        errorAttributes.put("data", pinpointErrorData);
    }
}
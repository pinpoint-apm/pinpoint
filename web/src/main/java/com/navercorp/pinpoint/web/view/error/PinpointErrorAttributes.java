package com.navercorp.pinpoint.web.view.error;

import org.springframework.boot.web.error.ErrorAttributeOptions;
import org.springframework.boot.web.servlet.error.DefaultErrorAttributes;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.WebRequest;
import java.util.Map;

@Component
public class PinpointErrorAttributes extends DefaultErrorAttributes {

    @Override
    public Map<String, Object> getErrorAttributes(WebRequest webRequest, ErrorAttributeOptions options) {
        Map<String, Object> errorAttributes = super.getErrorAttributes(webRequest, options);
        this.addCustomData(webRequest, errorAttributes);
        return errorAttributes;
    }

    private void addCustomData(WebRequest webRequest, Map<String, Object> errorAttributes) {
        PinpointErrorData pinpointErrorData = new PinpointErrorData(webRequest);
        errorAttributes.put("data", pinpointErrorData);
    }
}
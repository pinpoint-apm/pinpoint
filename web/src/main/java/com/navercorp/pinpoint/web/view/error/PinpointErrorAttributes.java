package com.navercorp.pinpoint.web.view.error;

import org.springframework.boot.web.error.ErrorAttributeOptions;
import org.springframework.boot.web.servlet.error.DefaultErrorAttributes;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;

@Component
public class PinpointErrorAttributes extends DefaultErrorAttributes {

    private PinpointErrorData pinpointErrorData;

    @Override
    public ModelAndView resolveException(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        this.pinpointErrorData = new PinpointErrorData(request);
        return super.resolveException(request, response, handler, ex);
    }

    @Override
    public Map<String, Object> getErrorAttributes(WebRequest webRequest, ErrorAttributeOptions options) {
        Map<String, Object> errorAttributes = super.getErrorAttributes(webRequest, options);
        this.addCustomData(errorAttributes);
        return errorAttributes;
    }

    private void addCustomData(Map<String, Object> errorAttributes) {
        errorAttributes.put("data", pinpointErrorData);
    }
}
package com.navercorp.pinpoint.web.view.error;

import com.navercorp.pinpoint.common.util.OsType;
import com.navercorp.pinpoint.common.util.OsUtils;
import org.springframework.boot.web.error.ErrorAttributeOptions;
import org.springframework.boot.web.servlet.error.DefaultErrorAttributes;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.WebRequest;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Map;

@Component
public class PinpointErrorAttributes extends DefaultErrorAttributes {
    public static final String ERROR_HOST_NAME = "UNKNOWN-HOST";
    private final String hostname;

    public PinpointErrorAttributes() {
        this.hostname = getHostName();
    }

    @Override
    public Map<String, Object> getErrorAttributes(WebRequest webRequest, ErrorAttributeOptions options) {
        Map<String, Object> errorAttributes = super.getErrorAttributes(webRequest, options);
        this.addCustomData(webRequest, errorAttributes);
        return errorAttributes;
    }

    private void addCustomData(WebRequest webRequest, Map<String, Object> errorAttributes) {
        PinpointErrorData pinpointErrorData = new PinpointErrorData(this.hostname, webRequest);
        errorAttributes.put("data", pinpointErrorData);
    }

    private String getHostName() {
        final String hostName = getHostNameFromEnv();
        if (hostName != null) {
            return hostName;
        }

        return getHostNameFromDns();
    }

    private String getHostNameFromEnv() {
        final OsType type = OsUtils.getType();
        if (OsType.WINDOW == type) {
            return System.getenv("COMPUTERNAME");
        } else {
            return System.getenv("HOSTNAME");
        }
    }

    private String getHostNameFromDns() {
        try {
            final InetAddress localHost = InetAddress.getLocalHost();
            return localHost.getHostName();
        } catch (UnknownHostException e) {
            return ERROR_HOST_NAME;
        }
    }
}
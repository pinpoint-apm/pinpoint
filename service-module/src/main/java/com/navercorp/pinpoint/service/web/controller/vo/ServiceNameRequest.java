package com.navercorp.pinpoint.service.web.controller.vo;

import com.navercorp.pinpoint.common.PinpointConstants;
import com.navercorp.pinpoint.common.util.IdValidateUtils;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class ServiceNameRequest {

    @NotBlank
    @Size(max = PinpointConstants.SERVICE_NAME_MAX_LEN)
    @Pattern(regexp = IdValidateUtils.ID_PATTERN_VALUE)
    private String serviceName;

    public ServiceNameRequest() {
    }

    public ServiceNameRequest(String serviceName) {
        this.serviceName = serviceName;
    }

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    @Override
    public String toString() {
        return "ServiceNameRequest{serviceName='" + serviceName + "'}";
    }
}

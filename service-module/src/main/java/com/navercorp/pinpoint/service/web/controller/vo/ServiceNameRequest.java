package com.navercorp.pinpoint.service.web.controller.vo;

public class ServiceNameRequest {

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

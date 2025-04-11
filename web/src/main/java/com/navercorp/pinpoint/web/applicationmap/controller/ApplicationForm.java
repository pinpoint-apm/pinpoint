package com.navercorp.pinpoint.web.applicationmap.controller;

import com.navercorp.pinpoint.common.trace.ServiceType;
import jakarta.validation.constraints.NotBlank;

public class ApplicationForm {

    public static final int UNDEFINED = ServiceType.UNDEFINED.getCode();

    @NotBlank
    private String applicationName;
    private int serviceTypeCode = UNDEFINED;
    private String serviceTypeName;

    public ApplicationForm() {
    }


    public void setApplicationName(String applicationName) {
        this.applicationName = applicationName;
    }

    public void setServiceTypeCode(int serviceTypeCode) {
        this.serviceTypeCode = serviceTypeCode;
    }

    public void setServiceTypeName(String serviceTypeName) {
        this.serviceTypeName = serviceTypeName;
    }

    public String getApplicationName() {
        return applicationName;
    }

    public int getServiceTypeCode() {
        return serviceTypeCode;
    }

    public String getServiceTypeName() {
        return serviceTypeName;
    }

    @Override
    public String toString() {
        return "ApplicationForm{" +
                "applicationName='" + applicationName + '\'' +
                ", serviceTypeCode=" + serviceTypeCode +
                ", serviceTypeName='" + serviceTypeName + '\'' +
                '}';
    }
}

package com.navercorp.pinpoint.collector.service;

public interface ApplicationServiceTypeService {

    void put(String applicationName, int serviceTypeCode);

    int getServiceTypeCode(String applicationName);
}
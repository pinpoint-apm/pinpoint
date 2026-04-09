package com.navercorp.pinpoint.collector.service;

import com.navercorp.pinpoint.common.trace.ServiceType;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.stereotype.Service;

@ConditionalOnMissingBean(value = ApplicationServiceTypeService.class, ignored = EmptyApplicationServiceTypeService.class)
@Service
public class EmptyApplicationServiceTypeService implements ApplicationServiceTypeService {

    @Override
    public void put(String applicationName, int serviceTypeCode) {
        // empty
    }

    @Override
    public int getServiceTypeCode(String applicationName) {
        return ServiceType.UNDEFINED.getCode();
    }
}
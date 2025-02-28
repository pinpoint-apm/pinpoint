package com.navercorp.pinpoint.collector.service;

import com.navercorp.pinpoint.common.server.vo.ServiceUid;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.stereotype.Service;

@Service
@ConditionalOnMissingBean(value = ServiceGroupService.class, ignored = DefaultServiceGroupService.class)
public class DefaultServiceGroupService implements ServiceGroupService {

    @Override
    public ServiceUid getServiceUid(String serviceName) {
        return ServiceUid.DEFAULT_SERVICE_UID;
    }
}

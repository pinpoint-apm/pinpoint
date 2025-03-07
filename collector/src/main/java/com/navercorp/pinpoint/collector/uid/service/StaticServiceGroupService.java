package com.navercorp.pinpoint.collector.uid.service;

import com.navercorp.pinpoint.common.server.uid.ServiceUid;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.stereotype.Service;

@Service
@ConditionalOnMissingBean(value = ServiceGroupService.class, ignored = StaticServiceGroupService.class)
public class StaticServiceGroupService implements ServiceGroupService {

    private final ServiceUid staticServiceUid;

    public StaticServiceGroupService(@Value("${pinpoint.collector.service.uid.static.value:0}") int staticServiceUid) {
        this.staticServiceUid = ServiceUid.of(staticServiceUid);
    }

    @Override
    public ServiceUid getServiceUid(String serviceName) {
        return staticServiceUid;
    }
}

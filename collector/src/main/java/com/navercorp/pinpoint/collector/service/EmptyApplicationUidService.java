package com.navercorp.pinpoint.collector.service;

import com.navercorp.pinpoint.common.server.vo.ApplicationUid;
import com.navercorp.pinpoint.common.server.vo.ServiceUid;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.stereotype.Service;

@Service
@ConditionalOnMissingBean(value = ApplicationUidService.class, ignored = EmptyApplicationUidService.class)
public class EmptyApplicationUidService implements ApplicationUidService {

    @Override
    public ApplicationUid getApplicationId(ServiceUid serviceUid, String applicationName) {
        return null;
    }

    @Override
    public ApplicationUid getOrCreateApplicationId(ServiceUid serviceUid, String applicationName) {
        return null;
    }
}

package com.navercorp.pinpoint.collector.uid.service;

import com.navercorp.pinpoint.common.server.uid.ServiceUid;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

@Service
@ConditionalOnProperty(name = "pinpoint.collector.v4.enable", havingValue = "false", matchIfMissing = true)
public class StaticServiceGroupService implements ServiceGroupService {
    private final Logger logger = LogManager.getLogger(this.getClass());

    private final ServiceUid staticServiceUid;

    public StaticServiceGroupService(@Value("${collector.service.uid.default.value:0}") int uid) {
        this.staticServiceUid = ServiceUid.of(uid);
        logger.info("StaticServiceGroupService initialized. {}", this.staticServiceUid);
    }

    @Override
    public ServiceUid getServiceUid(String serviceName) {
        return staticServiceUid;
    }
}

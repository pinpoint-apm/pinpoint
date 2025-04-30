package com.navercorp.pinpoint.web.uid.service;

import com.navercorp.pinpoint.common.server.uid.ApplicationUid;
import com.navercorp.pinpoint.common.server.uid.ServiceUid;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

@Service
@ConditionalOnProperty(name = "pinpoint.web.application.uid.enable", havingValue = "false", matchIfMissing = true)
public class EmptyApplicationUidService implements ApplicationUidService {
    private final Logger logger = LogManager.getLogger(this.getClass());

    public EmptyApplicationUidService() {
        logger.info("EmptyApplicationUidService initialized");
    }

    @Override
    public List<String> getApplicationNames(ServiceUid serviceUid) {
        return Collections.emptyList();
    }

    @Override
    public ApplicationUid getApplicationUid(ServiceUid serviceUid, String applicationName) {
        return null;
    }

    @Override
    public String getApplicationName(ServiceUid serviceUid, ApplicationUid applicationUid) {
        return null;
    }

    @Override
    public void deleteApplication(ServiceUid serviceUid, String applicationName) {

    }

    @Override
    public int cleanupEmptyApplication(ServiceUid serviceUid, long fromTimestamp) {
        return 0;
    }

    @Override
    public int cleanupInconsistentApplicationName(ServiceUid serviceUid) {
        return 0;
    }
}

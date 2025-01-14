package com.navercorp.pinpoint.collector.service;

import java.util.UUID;

public interface ServiceGroupService {
    UUID getServiceUid(String serviceName);
}

package com.navercorp.pinpoint.web.service;

import jakarta.annotation.Nullable;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public interface ServiceGroupService {

    void createServiceGroup(String serviceName, Map<String, String> tags);
    void deleteServiceGroup(String serviceName);

    List<String> selectAllServiceNames();
    String selectServiceName(UUID serviceUid);
    UUID selectServiceUid(String serviceName);

    Map<String, String> selectServiceTags(String serviceName);
    void insertServiceTag(String serviceName, String key, String value);
    void deleteServiceTag(String serviceName, String key);
}

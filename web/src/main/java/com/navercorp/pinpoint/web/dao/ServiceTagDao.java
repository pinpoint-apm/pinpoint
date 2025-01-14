package com.navercorp.pinpoint.web.dao;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public interface ServiceTagDao {

    Map<String, String> selectServiceTags(UUID serviceUid);

    void insertServiceTag(UUID serviceUid, Map<String, String> tags);

    void insertServiceTag(UUID serviceUid, String key, String value);

    void deleteServiceTag(UUID serviceUid, String key);

    void deleteAllServiceTags(UUID serviceUid);
}

package com.navercorp.pinpoint.collector.dao;

import java.util.UUID;

public interface ServiceUidDao {

    UUID selectServiceUid(String serviceName);
}
package com.navercorp.pinpoint.collector.dao;

public interface ApplicationDao {

    void insert(int serviceUid, String applicationName, int serviceTypeCode);
}

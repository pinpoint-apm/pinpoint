package com.navercorp.pinpoint.collector.dao;

import java.util.List;

public interface ApplicationDao {

    void insert(int serviceUid, String applicationName, int serviceTypeCode);

    List<Integer> selectServiceTypeCodes(int serviceUid, String applicationName);
}

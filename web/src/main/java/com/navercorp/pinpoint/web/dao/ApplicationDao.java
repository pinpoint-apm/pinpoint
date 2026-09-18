package com.navercorp.pinpoint.web.dao;

import com.navercorp.pinpoint.common.server.bo.Application;

import java.util.List;

public interface ApplicationDao {
    List<Application> getApplications(int serviceUid);

    List<Application> getApplications(int serviceUid, String applicationName);

    void deleteApplication(int serviceUid, String applicationName, int serviceTypeCode);

    void deleteApplication(int serviceUid, String applicationName, int serviceTypeCode, long timestamp);

    // only for table migration
    void insert(int serviceUid, String applicationName, int serviceTypeCode);
}

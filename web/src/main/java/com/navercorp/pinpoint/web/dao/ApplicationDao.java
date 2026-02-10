package com.navercorp.pinpoint.web.dao;

import com.navercorp.pinpoint.web.vo.Application;

import java.util.List;

public interface ApplicationDao {
    List<Application> getApplications(int serviceUid);

    List<Application> getApplications(int serviceUid, String applicationName);

    void deleteApplication(int serviceUid, String applicationName, int serviceTypeCode);

    //
    void insert(int serviceUid, String applicationName, int serviceTypeCode);
}

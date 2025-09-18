package com.navercorp.pinpoint.web.dao;

import com.navercorp.pinpoint.common.server.uid.ServiceUid;
import com.navercorp.pinpoint.web.vo.Application;

import java.util.List;

public interface ApplicationDao {
    List<Application> getApplications(ServiceUid serviceUid);

    List<Application> getApplications(ServiceUid serviceUid, String applicationName);

    void deleteApplication(ServiceUid serviceUid, String applicationName, int serviceTypeCode);

    //
    void insert(ServiceUid serviceUid, String applicationName, int serviceTypeCode);
}

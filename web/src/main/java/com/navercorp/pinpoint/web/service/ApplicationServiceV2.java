package com.navercorp.pinpoint.web.service;

import com.navercorp.pinpoint.common.server.uid.ServiceUid;
import com.navercorp.pinpoint.web.vo.Application;

import java.util.List;

public interface ApplicationServiceV2 {

    List<Application> getApplications(ServiceUid serviceUid);

    List<Application> getApplications(ServiceUid serviceUid, String applicationName);

    void deleteApplication(ServiceUid serviceUid, String applicationName, int serviceTypeCode);

}

package com.navercorp.pinpoint.web.dao;

import com.navercorp.pinpoint.common.server.vo.ApplicationUid;
import com.navercorp.pinpoint.common.server.vo.ServiceUid;

import java.util.List;

public interface ApplicationNameDao {

    List<String> selectApplicationNames(ServiceUid serviceUid);

    String selectApplicationName(ServiceUid serviceUid, ApplicationUid applicationUid);

    void deleteApplicationName(ServiceUid serviceUid, ApplicationUid applicationUid);
}

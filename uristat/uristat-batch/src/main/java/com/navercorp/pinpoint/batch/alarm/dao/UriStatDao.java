package com.navercorp.pinpoint.batch.alarm.dao;

import com.navercorp.pinpoint.batch.alarm.vo.PinotAlarmKey;
import com.navercorp.pinpoint.batch.alarm.vo.UriStatQueryParams;

import java.util.List;


public interface UriStatDao {
    double selectTotalCount(UriStatQueryParams params);
    double selectFailureCount(UriStatQueryParams params);
    double selectApdex(UriStatQueryParams params);
    double selectAvgResponse(UriStatQueryParams params);
    double selectMaxResponse(UriStatQueryParams params);
    boolean checkIfKeyExists(UriStatQueryParams params);

}

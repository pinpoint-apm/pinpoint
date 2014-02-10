package com.nhn.pinpoint.web.dao;

import com.nhn.pinpoint.web.applicationmap.rawdata.TransactionFlowStatistics;
import com.nhn.pinpoint.web.vo.Application;
import com.nhn.pinpoint.web.vo.Range;
import com.nhn.pinpoint.web.vo.RawResponseTime;

import java.util.List;
import java.util.Map;

/**
 * 
 * @author netspider
 * 
 */
public interface MapResponseDao {
    List<RawResponseTime> selectResponseTime(Application application, Range range);

}

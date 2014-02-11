package com.nhn.pinpoint.web.dao;

import com.nhn.pinpoint.web.vo.Application;
import com.nhn.pinpoint.web.vo.Range;
import com.nhn.pinpoint.web.vo.RawResponseTime;

import java.util.List;

/**
 * 
 * @author netspider
 * 
 */
public interface MapResponseDao {
    List<RawResponseTime> selectResponseTime(Application application, Range range);

}

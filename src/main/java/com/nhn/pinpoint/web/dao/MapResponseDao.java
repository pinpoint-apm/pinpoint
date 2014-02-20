package com.nhn.pinpoint.web.dao;

import com.nhn.pinpoint.web.vo.Application;
import com.nhn.pinpoint.web.vo.Range;
import com.nhn.pinpoint.web.vo.ResponseTime;

import java.util.List;

/**
 * 
 * @author netspider
 * 
 */
public interface MapResponseDao {
    List<ResponseTime> selectResponseTime(Application application, Range range);

}

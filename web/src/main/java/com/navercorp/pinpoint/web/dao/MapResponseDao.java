package com.navercorp.pinpoint.web.dao;

import com.navercorp.pinpoint.web.vo.Application;
import com.navercorp.pinpoint.web.vo.Range;
import com.navercorp.pinpoint.web.vo.ResponseTime;

import java.util.List;

/**
 * 
 * @author netspider
 * 
 */
public interface MapResponseDao {
    List<ResponseTime> selectResponseTime(Application application, Range range);

}

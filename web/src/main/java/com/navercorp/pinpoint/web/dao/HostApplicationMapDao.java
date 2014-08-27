package com.nhn.pinpoint.web.dao;

import com.nhn.pinpoint.web.service.map.AcceptApplication;
import com.nhn.pinpoint.web.vo.Application;
import com.nhn.pinpoint.web.vo.Range;

import java.util.Set;


/**
 *
 * @author emeroad
 * @author netspider
 * 
 */
public interface HostApplicationMapDao {
    @Deprecated
    Set<AcceptApplication> findAcceptApplicationName(String host, Range range);

    Set<AcceptApplication> findAcceptApplicationName(Application fromApplication, Range range);
}

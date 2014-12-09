package com.navercorp.pinpoint.web.dao;

import com.navercorp.pinpoint.web.service.map.AcceptApplication;
import com.navercorp.pinpoint.web.vo.Application;
import com.navercorp.pinpoint.web.vo.Range;

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

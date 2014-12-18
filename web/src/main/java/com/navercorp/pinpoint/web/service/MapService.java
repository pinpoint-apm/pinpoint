package com.navercorp.pinpoint.web.service;

import com.navercorp.pinpoint.web.applicationmap.ApplicationMap;
import com.navercorp.pinpoint.web.applicationmap.histogram.NodeHistogram;
import com.navercorp.pinpoint.web.vo.Application;
import com.navercorp.pinpoint.web.vo.Range;

/**
 * @author netspider
 */
public interface MapService {
    /**
     * 메인 화면의 서버 맵 조회.
     *
     * @param sourceApplication
     * @param range
     * @return
     */
    ApplicationMap selectApplicationMap(Application sourceApplication, Range range);

    @Deprecated
    NodeHistogram linkStatistics(Application sourceApplication, Application destinationApplication, Range range);
}

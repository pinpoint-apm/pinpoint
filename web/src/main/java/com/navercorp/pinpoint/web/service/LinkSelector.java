package com.navercorp.pinpoint.web.service;

import com.navercorp.pinpoint.web.applicationmap.rawdata.LinkDataDuplexMap;
import com.navercorp.pinpoint.web.vo.Application;
import com.navercorp.pinpoint.web.vo.Range;
import com.navercorp.pinpoint.web.vo.SearchOption;

/**
 * @author emeroad
 */
public interface LinkSelector {
    LinkDataDuplexMap select(Application sourceApplication, Range range, SearchOption searchOption);
}

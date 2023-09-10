package com.navercorp.pinpoint.web.dao;

import com.navercorp.pinpoint.common.server.util.time.Range;

import java.util.List;

public interface ApplicationIndexPerTimeDao {

    List<String> selectAgentIds(String applicationName, Range range);

}

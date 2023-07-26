package com.navercorp.pinpoint.web.dao;

import com.navercorp.pinpoint.common.server.util.time.Range;
import com.navercorp.pinpoint.web.vo.Application;

import java.util.List;
import java.util.Map;

public interface ApplicationIndexDaoProxy {
    List<Application> selectAllApplicationNames();

    List<Application> selectApplicationName(String applicationName);

    List<String> selectAgentIds(String applicationName);

    List<String> selectAgentIds(String applicationName, Range range);

    void deleteApplicationName(String applicationName);

    void deleteAgentIds(Map<String, List<String>> applicationAgentIdMap);

    void deleteAgentId(String applicationName, String agentId);
}

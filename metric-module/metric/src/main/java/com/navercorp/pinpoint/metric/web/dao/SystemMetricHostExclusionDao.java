package com.navercorp.pinpoint.metric.web.dao;

import java.util.List;

public interface SystemMetricHostExclusionDao {

    List<String> selectExcludedHostGroupNameList();

    void insertHostGroupExclusion(String hostGroupName);

    void deleteHostGroupExclusion(String hostGroupName);

    List<String> selectExcludedHostNameList(String hostGroupName);

    void insertHostExclusion(String hostGroupName, String hostName);

    void deleteHostExclusion(String hostGroupName, String hostName);

    void deleteHostExclusions(String hostGroupName);

    List<String> selectGroupNameListFromHostExclusion();
}
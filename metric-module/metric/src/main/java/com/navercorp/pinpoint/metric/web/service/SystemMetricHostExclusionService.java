package com.navercorp.pinpoint.metric.web.service;

import com.navercorp.pinpoint.metric.web.view.SystemMetricHostGroupInfo;
import com.navercorp.pinpoint.metric.web.view.SystemMetricHostInfo;

import java.util.List;

public interface SystemMetricHostExclusionService {

    List<String> getHostGroupNameList(String tenantId);

    SystemMetricHostGroupInfo getHostGroupInfo(String tenantId, String hostGroupName);

    List<SystemMetricHostInfo> getHostInfoList(String tenantId, String hostGroupName, String orderBy);

    void insertHostGroupExclusion(String hostGroupName);

    void deleteHostGroupExclusion(String hostGroupName);

    void insertHostExclusion(String hostGroupName, String hostName);

    void deleteHostExclusion(String hostGroupName, String hostName);

    void deleteUnusedGroupExclusions(String tenantId);
}
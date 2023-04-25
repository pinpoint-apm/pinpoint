package com.navercorp.pinpoint.metric.web.service;

import com.navercorp.pinpoint.metric.web.view.SystemMetricHostGroupInfo;
import com.navercorp.pinpoint.metric.web.view.SystemMetricHostInfo;

import java.util.List;

public interface SystemMetricHostExclusionService {

    SystemMetricHostGroupInfo getHostGroupInfo(String tenantId, String hostGroupName);

    List<SystemMetricHostInfo> getHostInfoList(String tenantId, String hostGroupName);

    void insertHostGroupExclusion(String hostGroupName);

    void deleteHostGroupExclusion(String hostGroupName);

    void insertHostExclusion(String hostGroupName, String hostName);

    void deleteHostExclusion(String hostGroupName, String hostName);

    void deleteUnusedHostExclusions(String tenantId, String hostGroupName);

    void deleteUnusedGroupExclusions(String tenantId);
}
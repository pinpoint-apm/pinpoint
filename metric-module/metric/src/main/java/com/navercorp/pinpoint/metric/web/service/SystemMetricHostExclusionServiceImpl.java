package com.navercorp.pinpoint.metric.web.service;

import com.navercorp.pinpoint.metric.web.dao.SystemMetricHostExclusionDao;
import com.navercorp.pinpoint.metric.web.dao.SystemMetricHostInfoDao;
import com.navercorp.pinpoint.metric.web.view.SystemMetricHostGroupInfo;
import com.navercorp.pinpoint.metric.web.view.SystemMetricHostInfo;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

@Service
@Transactional(rollbackFor = {Exception.class})
public class SystemMetricHostExclusionServiceImpl implements SystemMetricHostExclusionService {

    private final SystemMetricHostInfoDao systemMetricHostInfoDao;
    private final SystemMetricHostExclusionDao systemMetricHostExclusionDao;

    public SystemMetricHostExclusionServiceImpl(SystemMetricHostInfoDao systemMetricHostInfoDao, SystemMetricHostExclusionDao systemMetricHostExclusionDao) {
        this.systemMetricHostInfoDao = Objects.requireNonNull(systemMetricHostInfoDao, "systemMetricHostInfoDao");
        this.systemMetricHostExclusionDao = Objects.requireNonNull(systemMetricHostExclusionDao, "systemMetricHostExclusionDao");
    }

    @Override
    public SystemMetricHostGroupInfo getHostGroupInfo(String tenantId, String hostGroupName) {
        boolean excluded = systemMetricHostExclusionDao.selectExcludedHostGroupNameList().contains(hostGroupName);
        return new SystemMetricHostGroupInfo(hostGroupName, excluded);
    }

    @Override
    public List<SystemMetricHostInfo> getHostInfoList(String tenantId, String hostGroupName) {
        List<SystemMetricHostInfo> result = new ArrayList<>();
        List<String> hostNames = systemMetricHostInfoDao.selectHostList(tenantId, hostGroupName);
        List<String> excludedHostNames = systemMetricHostExclusionDao.selectExcludedHostNameList(hostGroupName);
        for (String hostName : hostNames) {
            result.add(new SystemMetricHostInfo(hostName, excludedHostNames.contains(hostName)));
        }
        for (String excludedHostName : excludedHostNames) {
            if (!hostNames.contains(excludedHostName)) {
                result.add(new SystemMetricHostInfo(excludedHostName, true));
            }
        }

        result.sort(Comparator.comparing(SystemMetricHostInfo::getHostName));
        return result;
    }

    @Override
    public void insertHostGroupExclusion(String hostGroupName) {
        systemMetricHostExclusionDao.insertHostGroupExclusion(hostGroupName);
    }

    @Override
    public void deleteHostGroupExclusion(String hostGroupName) {
        systemMetricHostExclusionDao.deleteHostGroupExclusion(hostGroupName);
    }

    @Override
    public void insertHostExclusion(String hostGroupName, String hostName) {
        systemMetricHostExclusionDao.insertHostExclusion(hostGroupName, hostName);
    }

    @Override
    public void deleteHostExclusion(String hostGroupName, String hostName) {
        systemMetricHostExclusionDao.deleteHostExclusion(hostGroupName, hostName);
    }

    @Override
    public void deleteUnusedHostExclusions(String tenantId, String hostGroupName) {
        List<String> hostNames = systemMetricHostInfoDao.selectHostList(tenantId, hostGroupName);
        List<String> excludedHostNames = systemMetricHostExclusionDao.selectExcludedHostNameList(hostGroupName);
        for (String excludedHostName : excludedHostNames) {
            if (!hostNames.contains(excludedHostName)) {
                systemMetricHostExclusionDao.deleteHostExclusion(hostGroupName, excludedHostName);
            }
        }
    }

    @Override
    public void deleteUnusedGroupExclusions(String tenantId) {
        List<String> hostGroupNames = systemMetricHostInfoDao.selectHostGroupNameList(tenantId);
        List<String> hostGroupExclusionNames = systemMetricHostExclusionDao.selectExcludedHostGroupNameList();
        for (String excludedHostGroupName : hostGroupExclusionNames) {
            if (!hostGroupNames.contains(excludedHostGroupName)) {
                systemMetricHostExclusionDao.deleteHostGroupExclusion(excludedHostGroupName);
            }
        }
        List<String> hostExclusionGroupNames = systemMetricHostExclusionDao.selectGroupNameListFromHostExclusion();
        for (String hostExclusionGroupName : hostExclusionGroupNames) {
            if (!hostGroupNames.contains(hostExclusionGroupName)) {
                systemMetricHostExclusionDao.deleteHostExclusions(hostExclusionGroupName);
            }
        }
    }
}
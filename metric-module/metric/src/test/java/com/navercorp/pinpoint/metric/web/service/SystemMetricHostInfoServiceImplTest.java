package com.navercorp.pinpoint.metric.web.service;

import com.navercorp.pinpoint.metric.web.dao.SystemMetricHostExclusionDao;
import com.navercorp.pinpoint.metric.web.dao.SystemMetricHostInfoDao;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * @author minwoo.jung
 */

@ExtendWith(MockitoExtension.class)
public class SystemMetricHostInfoServiceImplTest {
    SystemMetricHostInfoService systemMetricHostInfoService;

    @Mock
    SystemMetricHostInfoDao systemMetricHostInfoDao;

    @Mock
    YMLSystemMetricBasicGroupManager ymlSystemMetricBasicGroupManager;

    @Mock
    SystemMetricHostExclusionDao systemMetricHostExclusionDao;

    @BeforeEach
    void setUp() {
        systemMetricHostInfoService = new SystemMetricHostInfoServiceImpl(systemMetricHostInfoDao, ymlSystemMetricBasicGroupManager, systemMetricHostExclusionDao);
    }

    @Test
    public void hostGroupExclusionTest() {
        List<String> hostGroupNames = new ArrayList<>();
        hostGroupNames.add("hostGroupName1");
        hostGroupNames.add("hostGroupName2");

        List<String> excludedHostGroupNames = new ArrayList<>();
        excludedHostGroupNames.add("hostGroupName2");

        when(systemMetricHostInfoDao.selectHostGroupNameList("tenantId")).thenReturn(hostGroupNames);
        when(systemMetricHostExclusionDao.selectExcludedHostGroupNameList()).thenReturn(excludedHostGroupNames);

        List<String> result = systemMetricHostInfoService.getHostGroupNameList("tenantId");

        verify(systemMetricHostInfoDao).selectHostGroupNameList("tenantId");
        verify(systemMetricHostExclusionDao).selectExcludedHostGroupNameList();
        assertThat(result).isEqualTo(List.of("hostGroupName1"));
    }

    @Test
    public void hostGroupExclusionErrorTest() {
        List<String> hostGroupNames = new ArrayList<>();
        hostGroupNames.add("hostGroupName1");
        hostGroupNames.add("hostGroupName2");

        List<String> excludedHostGroupNames = new ArrayList<>();
        excludedHostGroupNames.add("hostGroupName2");

        when(systemMetricHostInfoDao.selectHostGroupNameList("tenantId")).thenReturn(hostGroupNames);
        when(systemMetricHostExclusionDao.selectExcludedHostGroupNameList()).thenThrow(new RuntimeException("Test Exception"));

        List<String> result = systemMetricHostInfoService.getHostGroupNameList("tenantId");

        verify(systemMetricHostInfoDao).selectHostGroupNameList("tenantId");
        verify(systemMetricHostExclusionDao).selectExcludedHostGroupNameList();
        assertThat(result).isEqualTo(hostGroupNames);
    }

    @Test
    public void hostExclusionTest() {
        List<String> hostNames = new ArrayList<>();
        hostNames.add("hostName1");
        hostNames.add("hostName2");
        when(systemMetricHostInfoDao.selectHostList("tenantId", "hostGroupName")).thenReturn(hostNames);
        when(systemMetricHostExclusionDao.selectExcludedHostNameList("hostGroupName")).thenReturn(List.of("hostName1", "hostName3"));

        List<String> result = systemMetricHostInfoService.getHostList("tenantId", "hostGroupName");

        verify(systemMetricHostInfoDao).selectHostList("tenantId", "hostGroupName");
        verify(systemMetricHostExclusionDao).selectExcludedHostNameList("hostGroupName");
        assertThat(result).isEqualTo(List.of("hostName2"));
    }

    @Test
    public void hostExclusionErrorTest() {
        List<String> hostNames = new ArrayList<>();
        hostNames.add("hostName1");
        hostNames.add("hostName2");
        when(systemMetricHostInfoDao.selectHostList("tenantId", "hostGroupName")).thenReturn(hostNames);
        when(systemMetricHostExclusionDao.selectExcludedHostNameList("hostGroupName")).thenThrow(new RuntimeException("Test Exception"));

        List<String> result = systemMetricHostInfoService.getHostList("tenantId", "hostGroupName");

        verify(systemMetricHostInfoDao).selectHostList("tenantId", "hostGroupName");
        verify(systemMetricHostExclusionDao).selectExcludedHostNameList("hostGroupName");
        assertThat(result).isEqualTo(hostNames);
    }
}
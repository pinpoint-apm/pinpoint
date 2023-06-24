package com.navercorp.pinpoint.metric.web.service;

import com.navercorp.pinpoint.metric.web.dao.SystemMetricHostExclusionDao;
import com.navercorp.pinpoint.metric.web.dao.SystemMetricHostInfoDao;
import com.navercorp.pinpoint.metric.web.view.SystemMetricHostGroupInfo;
import com.navercorp.pinpoint.metric.web.view.SystemMetricHostInfo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Comparator;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class SystemMetricHostExclusionServiceImplTest {

    SystemMetricHostExclusionService systemMetricHostExclusionService;

    @Mock
    SystemMetricHostInfoDao systemMetricHostInfoDao;

    @Mock
    SystemMetricHostExclusionDao systemMetricHostExclusionDao;

    @BeforeEach
    void setUp() {
        systemMetricHostExclusionService = new SystemMetricHostExclusionServiceImpl(systemMetricHostInfoDao, systemMetricHostExclusionDao);
    }

    @Test
    public void getHostGroupExclusionInfoTest() {
        when(systemMetricHostExclusionDao.selectExcludedHostGroupNameList()).thenReturn(List.of("excludedHostGroupName"));
        SystemMetricHostGroupInfo hostGroupResult = systemMetricHostExclusionService.getHostGroupInfo("tenantId", "hostGroupName");

        assertThat(hostGroupResult.getHostGroupName()).isEqualTo("hostGroupName");
        assertThat(hostGroupResult.isExcluded()).isFalse();
    }

    @Test
    public void getEmptyHostGroupExclusionInfoTest() {
        when(systemMetricHostExclusionDao.selectExcludedHostGroupNameList()).thenReturn(List.of());

        SystemMetricHostGroupInfo hostGroupResult = systemMetricHostExclusionService.getHostGroupInfo("tenantId", "hostGroupName");

        assertThat(hostGroupResult.getHostGroupName()).isEqualTo("hostGroupName");
        assertThat(hostGroupResult.isExcluded()).isFalse();
    }

    @Test
    void getExcludedHostInfoTest() {
        when(systemMetricHostInfoDao.selectHostList("tenantId", "hostGroupName")).thenReturn(List.of("hostName", "excludedHostName"));
        when(systemMetricHostExclusionDao.selectExcludedHostNameList("hostGroupName")).thenReturn(List.of("excludedHostName", "anotherExcludedHostName"));
        List<SystemMetricHostInfo> hostResultList = systemMetricHostExclusionService.getHostInfoList("tenantId", "hostGroupName", "hostName");

        hostResultList.sort(Comparator.comparing(SystemMetricHostInfo::getHostName));
        assertThat(hostResultList.get(0).getHostName()).isEqualTo("anotherExcludedHostName");
        assertThat(hostResultList.get(0).isExcluded()).isTrue();
        assertThat(hostResultList.get(1).getHostName()).isEqualTo("excludedHostName");
        assertThat(hostResultList.get(1).isExcluded()).isTrue();
        assertThat(hostResultList.get(2).getHostName()).isEqualTo("hostName");
        assertThat(hostResultList.get(2).isExcluded()).isFalse();
    }

    @Test
    public void deleteUnusedGroupExclusionsTest() {
        when(systemMetricHostExclusionDao.selectExcludedHostGroupNameList()).thenReturn(List.of("liveHostGroupName", "deadHostGroupName1"));
        when(systemMetricHostExclusionDao.selectGroupNameListFromHostExclusion()).thenReturn(List.of("liveHostGroupName", "deadHostGroupName2"));
        when(systemMetricHostInfoDao.selectHostGroupNameList("tenantId")).thenReturn(List.of("liveHostGroupName"));

        lenient().doNothing().when(systemMetricHostExclusionDao).deleteHostGroupExclusion("liveHostGroupName");
        lenient().doNothing().when(systemMetricHostExclusionDao).deleteHostExclusions("liveHostGroupName");
        doNothing().when(systemMetricHostExclusionDao).deleteHostGroupExclusion("deadHostGroupName1");
        doNothing().when(systemMetricHostExclusionDao).deleteHostExclusions("deadHostGroupName2");

        systemMetricHostExclusionService.deleteUnusedGroupExclusions("tenantId");

        verify(systemMetricHostExclusionDao, never()).deleteHostGroupExclusion("liveHostGroupName");
        verify(systemMetricHostExclusionDao, never()).deleteHostExclusions("liveHostGroupName");
        verify(systemMetricHostExclusionDao).deleteHostGroupExclusion("deadHostGroupName1");
        verify(systemMetricHostExclusionDao).deleteHostExclusions("deadHostGroupName2");
    }
}

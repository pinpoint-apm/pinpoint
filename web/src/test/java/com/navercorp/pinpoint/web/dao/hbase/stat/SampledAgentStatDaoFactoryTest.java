/*
 * Copyright 2016 Naver Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.navercorp.pinpoint.web.dao.hbase.stat;

import com.navercorp.pinpoint.common.hbase.HBaseAdminTemplate;
import com.navercorp.pinpoint.common.hbase.HBaseTables;
import com.navercorp.pinpoint.web.dao.SampledAgentStatDao;
import com.navercorp.pinpoint.web.dao.hbase.stat.compatibility.HbaseSampledAgentStatDualReadDao;
import com.navercorp.pinpoint.web.vo.stat.SampledAgentStatDataPoint;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.test.util.ReflectionTestUtils;

import static org.mockito.Mockito.when;

/**
 * @author HyunGil Jeong
 */
@RunWith(MockitoJUnitRunner.class)
public class SampledAgentStatDaoFactoryTest {

    @Mock
    private SampledAgentStatDao<SampledAgentStatDataPoint> v1;

    @Mock
    private SampledAgentStatDao<SampledAgentStatDataPoint> v2;

    @Mock
    private HBaseAdminTemplate adminTemplate;

    @InjectMocks
    private TestSampledAgentStatDaoFactory sampledAgentStatDaoFactory = new TestSampledAgentStatDaoFactory();

    private final HbaseSampledAgentStatDualReadDao<SampledAgentStatDataPoint> compatibilityDao = new TestSampledAgentStatDualReadDao(v1, v2);

    private static class TestSampledAgentStatDualReadDao extends HbaseSampledAgentStatDualReadDao<SampledAgentStatDataPoint> {
        public TestSampledAgentStatDualReadDao(SampledAgentStatDao<SampledAgentStatDataPoint> master, SampledAgentStatDao<SampledAgentStatDataPoint> slave) {
            super(master, slave);
        }
    }

    private class TestSampledAgentStatDaoFactory extends SampledAgentStatDaoFactory<SampledAgentStatDataPoint, SampledAgentStatDao<SampledAgentStatDataPoint>> {
        @Override
        HbaseSampledAgentStatDualReadDao<SampledAgentStatDataPoint> getCompatibilityDao(
                SampledAgentStatDao<SampledAgentStatDataPoint> v1,
                SampledAgentStatDao<SampledAgentStatDataPoint> v2) {
            return compatibilityDao;
        }
    }

    @Test
    public void v2Mode_v2TableExists() throws Exception {
        // Given
        final SampledAgentStatDao<SampledAgentStatDataPoint> expectedDao = v2;
        final String mode = "v2";
        ReflectionTestUtils.setField(sampledAgentStatDaoFactory, "mode", mode);
        when(adminTemplate.tableExists(HBaseTables.AGENT_STAT_VER2)).thenReturn(true);
        // When
        SampledAgentStatDao<SampledAgentStatDataPoint> actualDao = sampledAgentStatDaoFactory.getDao();
        // Then
        Assert.assertEquals(expectedDao, actualDao);
    }

    @Test(expected = IllegalStateException.class)
    public void v2Mode_v2TableDoesNotExist() throws Exception {
        // Given
        final String mode = "v2";
        ReflectionTestUtils.setField(sampledAgentStatDaoFactory, "mode", mode);
        when(adminTemplate.tableExists(HBaseTables.AGENT_STAT_VER2)).thenReturn(false);
        // When
        sampledAgentStatDaoFactory.getDao();
        // Then
        Assert.fail("Should have thrown IllegalStateException.");
    }

    @Test
    public void compatibiltyMode_bothTablesExist() throws Exception {
        // Given
        final HbaseSampledAgentStatDualReadDao<SampledAgentStatDataPoint> expectedDao = compatibilityDao;
        final String mode = "compatibilityMode";
        ReflectionTestUtils.setField(sampledAgentStatDaoFactory, "mode", mode);
        when(adminTemplate.tableExists(HBaseTables.AGENT_STAT)).thenReturn(true);
        when(adminTemplate.tableExists(HBaseTables.AGENT_STAT_VER2)).thenReturn(true);
        // When
        SampledAgentStatDao<SampledAgentStatDataPoint> actualDao = sampledAgentStatDaoFactory.getDao();
        // Then
        Assert.assertEquals(expectedDao, actualDao);
    }

    @Test(expected = IllegalStateException.class)
    public void compatibilityMode_v1TableDoesNotExist() throws Exception {
        // Given
        final String mode = "compatibilityMode";
        ReflectionTestUtils.setField(sampledAgentStatDaoFactory, "mode", mode);
        when(adminTemplate.tableExists(HBaseTables.AGENT_STAT)).thenReturn(false);
        when(adminTemplate.tableExists(HBaseTables.AGENT_STAT_VER2)).thenReturn(true);
        // When
        sampledAgentStatDaoFactory.getDao();
        // Then
        Assert.fail("Should have thrown IllegalStateException.");
    }

    @Test(expected = IllegalStateException.class)
    public void compatibilityMode_v2TableDoesNotExist() throws Exception {
        // Given
        final String mode = "compatibilityMode";
        ReflectionTestUtils.setField(sampledAgentStatDaoFactory, "mode", mode);
        when(adminTemplate.tableExists(HBaseTables.AGENT_STAT)).thenReturn(true);
        when(adminTemplate.tableExists(HBaseTables.AGENT_STAT_VER2)).thenReturn(false);
        // When
        sampledAgentStatDaoFactory.getDao();
        // Then
        Assert.fail("Should have thrown IllegalStateException.");
    }

    @Test(expected = IllegalStateException.class)
    public void compatibilityMode_bothTablesDoNotExist() throws Exception {
        // Given
        final String mode = "compatibilityMode";
        ReflectionTestUtils.setField(sampledAgentStatDaoFactory, "mode", mode);
        when(adminTemplate.tableExists(HBaseTables.AGENT_STAT)).thenReturn(false);
        when(adminTemplate.tableExists(HBaseTables.AGENT_STAT_VER2)).thenReturn(false);
        // When
        sampledAgentStatDaoFactory.getDao();
        // Then
        Assert.fail("Should have thrown IllegalStateException.");
    }
}

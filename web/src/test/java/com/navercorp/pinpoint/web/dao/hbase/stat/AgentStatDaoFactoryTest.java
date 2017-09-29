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
import com.navercorp.pinpoint.common.server.bo.stat.AgentStatDataPoint;
import com.navercorp.pinpoint.web.dao.hbase.stat.compatibility.HbaseAgentStatDualReadDao;
import com.navercorp.pinpoint.web.dao.stat.AgentStatDao;
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
public class AgentStatDaoFactoryTest {

    @Mock
    private AgentStatDao<AgentStatDataPoint> v1;

    @Mock
    private AgentStatDao<AgentStatDataPoint> v2;

    @Mock
    private HBaseAdminTemplate adminTemplate;

    @InjectMocks
    private TestAgentStatDaoFactory agentStatDaoFactory = new TestAgentStatDaoFactory();

    private final HbaseAgentStatDualReadDao<AgentStatDataPoint> compatibilityDao = new TestAgentStatDualReadDao(v1, v2);

    private static class TestAgentStatDualReadDao extends HbaseAgentStatDualReadDao<AgentStatDataPoint> {
        public TestAgentStatDualReadDao(AgentStatDao<AgentStatDataPoint> master, AgentStatDao<AgentStatDataPoint> slave) {
            super(master, slave);
        }
    }

    private class TestAgentStatDaoFactory extends AgentStatDaoFactory<AgentStatDataPoint, AgentStatDao<AgentStatDataPoint>> {
        @Override
        HbaseAgentStatDualReadDao<AgentStatDataPoint> getCompatibilityDao(
                AgentStatDao<AgentStatDataPoint> v1,
                AgentStatDao<AgentStatDataPoint> v2) {
            return compatibilityDao;
        }
    }

    @Test
    public void v2Mode_v2TableExists() throws Exception {
        // Given
        final AgentStatDao<AgentStatDataPoint> expectedDao = v2;
        final String mode = "v2";
        ReflectionTestUtils.setField(agentStatDaoFactory, "mode", mode);
        when(adminTemplate.tableExists(HBaseTables.AGENT_STAT_VER2)).thenReturn(true);
        // When
        AgentStatDao<AgentStatDataPoint> actualDao = agentStatDaoFactory.getDao();
        // Then
        Assert.assertEquals(expectedDao, actualDao);
    }

    @Test(expected = IllegalStateException.class)
    public void v2Mode_v2TableDoesNotExist() throws Exception {
        // Given
        final String mode = "v2";
        ReflectionTestUtils.setField(agentStatDaoFactory, "mode", mode);
        when(adminTemplate.tableExists(HBaseTables.AGENT_STAT_VER2)).thenReturn(false);
        // When
        agentStatDaoFactory.getDao();
        // Then
        Assert.fail("Should have thrown IllegalStateException.");
    }

    @Test
    public void compatibilityMode_bothTablesExist() throws Exception {
        // Given
        final HbaseAgentStatDualReadDao<AgentStatDataPoint> expectedDao = compatibilityDao;
        final String mode = "compatibilityMode";
        ReflectionTestUtils.setField(agentStatDaoFactory, "mode", mode);
        when(adminTemplate.tableExists(HBaseTables.AGENT_STAT)).thenReturn(true);
        when(adminTemplate.tableExists(HBaseTables.AGENT_STAT_VER2)).thenReturn(true);
        // When
        AgentStatDao<AgentStatDataPoint> actualDao = agentStatDaoFactory.getDao();
        // Then
        Assert.assertEquals(expectedDao, actualDao);
    }

    @Test(expected = IllegalStateException.class)
    public void compatibilityMode_v1TableDoesNotExist() throws Exception {
        // Given
        final String mode = "compatibilityMode";
        ReflectionTestUtils.setField(agentStatDaoFactory, "mode", mode);
        when(adminTemplate.tableExists(HBaseTables.AGENT_STAT)).thenReturn(false);
        when(adminTemplate.tableExists(HBaseTables.AGENT_STAT_VER2)).thenReturn(true);
        // When
        agentStatDaoFactory.getDao();
        // Then
        Assert.fail("Should have thrown IllegalStateException.");
    }

    @Test(expected = IllegalStateException.class)
    public void compatibilityMode_v2TableDoesNotExist() throws Exception {
        // Given
        final String mode = "compatibilityMode";
        ReflectionTestUtils.setField(agentStatDaoFactory, "mode", mode);
        when(adminTemplate.tableExists(HBaseTables.AGENT_STAT)).thenReturn(true);
        when(adminTemplate.tableExists(HBaseTables.AGENT_STAT_VER2)).thenReturn(false);
        // When
        agentStatDaoFactory.getDao();
        // Then
        Assert.fail("Should have thrown IllegalStateException.");
    }

    @Test(expected = IllegalStateException.class)
    public void compatibilityMode_bothTablesDoNotExist() throws Exception {
        // Given
        final String mode = "compatibilityMode";
        ReflectionTestUtils.setField(agentStatDaoFactory, "mode", mode);
        when(adminTemplate.tableExists(HBaseTables.AGENT_STAT)).thenReturn(false);
        when(adminTemplate.tableExists(HBaseTables.AGENT_STAT_VER2)).thenReturn(false);
        // When
        agentStatDaoFactory.getDao();
        // Then
        Assert.fail("Should have thrown IllegalStateException.");
    }
}

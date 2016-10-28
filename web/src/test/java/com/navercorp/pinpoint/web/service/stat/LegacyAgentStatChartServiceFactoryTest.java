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

package com.navercorp.pinpoint.web.service.stat;

import com.navercorp.pinpoint.common.hbase.HBaseAdminTemplate;
import com.navercorp.pinpoint.common.hbase.HBaseTables;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.test.util.ReflectionTestUtils;

import static org.mockito.Mockito.when;

/**
 * @author HyunGil Jeong
 */
@Deprecated
@RunWith(MockitoJUnitRunner.class)
public class LegacyAgentStatChartServiceFactoryTest {

    @Mock
    private LegacyAgentStatChartService v1;

    @Mock
    private LegacyAgentStatChartService v2;

    @Mock
    private LegacyAgentStatChartService compatibility;

    @Mock
    private HBaseAdminTemplate adminTemplate;

    @InjectMocks
    private LegacyAgentStatChartServiceFactory serviceFactory = new LegacyAgentStatChartServiceFactory();

    @Test
    public void v1Mode_v1TableExists() throws Exception {
        // Given
        final LegacyAgentStatChartService expectedService = v1;
        final String mode = "v1";
        ReflectionTestUtils.setField(serviceFactory, "mode", mode);
        when(adminTemplate.tableExists(HBaseTables.AGENT_STAT)).thenReturn(true);
        // When
        LegacyAgentStatChartService actualService = serviceFactory.getObject();
        // Then
        Assert.assertEquals(expectedService, actualService);
    }

    @Test(expected = IllegalStateException.class)
    public void v1Mode_v1TableDoesNotExist() throws Exception {
        // Given
        final String mode = "v1";
        ReflectionTestUtils.setField(serviceFactory, "mode", mode);
        when(adminTemplate.tableExists(HBaseTables.AGENT_STAT)).thenReturn(false);
        // When
        serviceFactory.getObject();
        // Then
        Assert.fail("Should have thrown IllegalStateException.");
    }

    @Test
    public void v2Mode_v2TableExists() throws Exception {
        // Given
        final LegacyAgentStatChartService expectedService = v2;
        final String mode = "v2";
        ReflectionTestUtils.setField(serviceFactory, "mode", mode);
        when(adminTemplate.tableExists(HBaseTables.AGENT_STAT_VER2)).thenReturn(true);
        // When
        LegacyAgentStatChartService actualService = serviceFactory.getObject();
        // Then
        Assert.assertEquals(expectedService, actualService);
    }

    @Test(expected = IllegalStateException.class)
    public void v2Mode_v2TableDoesNotExist() throws Exception {
        // Given
        final String mode = "v2";
        ReflectionTestUtils.setField(serviceFactory, "mode", mode);
        when(adminTemplate.tableExists(HBaseTables.AGENT_STAT_VER2)).thenReturn(false);
        // When
        serviceFactory.getObject();
        // Then
        Assert.fail("Should have thrown IllegalStateException.");
    }

    @Test
    public void compatibilityMode_bothTablesExist() throws Exception {
        // Given
        final LegacyAgentStatChartService expectedService = compatibility;
        final String mode = "compatibilityMode";
        ReflectionTestUtils.setField(serviceFactory, "mode", mode);
        when(adminTemplate.tableExists(HBaseTables.AGENT_STAT)).thenReturn(true);
        when(adminTemplate.tableExists(HBaseTables.AGENT_STAT_VER2)).thenReturn(true);
        // When
        LegacyAgentStatChartService actualService = serviceFactory.getObject();
        // Then
        Assert.assertEquals(expectedService, actualService);
    }

    @Test(expected = IllegalStateException.class)
    public void compatibilityMode_v1TableDoesNotExist() throws Exception {
        // Given
        final String mode = "compatibilityMode";
        ReflectionTestUtils.setField(serviceFactory, "mode", mode);
        when(adminTemplate.tableExists(HBaseTables.AGENT_STAT)).thenReturn(false);
        when(adminTemplate.tableExists(HBaseTables.AGENT_STAT_VER2)).thenReturn(true);
        // When
        serviceFactory.getObject();
        // Then
        Assert.fail("Should have thrown IllegalStateException.");
    }

    @Test(expected = IllegalStateException.class)
    public void compatibilityMode_v2TableDoesNotExist() throws Exception {
        // Given
        final String mode = "compatibilityMode";
        ReflectionTestUtils.setField(serviceFactory, "mode", mode);
        when(adminTemplate.tableExists(HBaseTables.AGENT_STAT)).thenReturn(true);
        when(adminTemplate.tableExists(HBaseTables.AGENT_STAT_VER2)).thenReturn(false);
        // When
        serviceFactory.getObject();
        // Then
        Assert.fail("Should have thrown IllegalStateException.");
    }

    @Test(expected = IllegalStateException.class)
    public void compatibilityMode_bothTablesDoNotExist() throws Exception {
        // Given
        final String mode = "compatibilityMode";
        ReflectionTestUtils.setField(serviceFactory, "mode", mode);
        when(adminTemplate.tableExists(HBaseTables.AGENT_STAT)).thenReturn(false);
        when(adminTemplate.tableExists(HBaseTables.AGENT_STAT_VER2)).thenReturn(false);
        // When
        serviceFactory.getObject();
        // Then
        Assert.fail("Should have thrown IllegalStateException.");
    }
}

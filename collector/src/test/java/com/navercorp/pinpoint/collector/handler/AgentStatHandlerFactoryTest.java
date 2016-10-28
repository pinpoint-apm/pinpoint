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

package com.navercorp.pinpoint.collector.handler;

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
@RunWith(MockitoJUnitRunner.class)
public class AgentStatHandlerFactoryTest {

    @Mock
    private AgentStatHandler v1;

    @Mock
    private AgentStatHandlerV2 v2;

    @Mock
    private HBaseAdminTemplate adminTemplate;

    @InjectMocks
    private final AgentStatHandlerFactory handlerFactory = new AgentStatHandlerFactory();

    @Test
    public void v1Mode_v1TableExists() throws Exception {
        // Given
        final Handler expectedHandler = v1;
        final String mode = "v1";
        ReflectionTestUtils.setField(handlerFactory, "mode", mode);
        when(adminTemplate.tableExists(HBaseTables.AGENT_STAT)).thenReturn(true);
        // When
        Handler actualHandler = handlerFactory.getObject();
        // Then
        Assert.assertEquals(expectedHandler, actualHandler);
    }

    @Test(expected = IllegalStateException.class)
    public void v1Mode_v1TableDoesNotExist() throws Exception {
        // Given
        final String mode = "v1";
        ReflectionTestUtils.setField(handlerFactory, "mode", mode);
        when(adminTemplate.tableExists(HBaseTables.AGENT_STAT)).thenReturn(false);
        // When
        handlerFactory.getObject();
        // Then
        Assert.fail("Should have thrown IllegalStateException.");
    }

    @Test
    public void v2Mode_v2TableExists() throws Exception {
        // Given
        final Handler expectedHandler = v2;
        final String mode = "v2";
        ReflectionTestUtils.setField(handlerFactory, "mode", mode);
        when(adminTemplate.tableExists(HBaseTables.AGENT_STAT_VER2)).thenReturn(true);
        // When
        Handler actualHandler = handlerFactory.getObject();
        // Then
        Assert.assertEquals(expectedHandler, actualHandler);
    }

    @Test(expected = IllegalStateException.class)
    public void v2Mode_v2TablesDoesNotExist() throws Exception {
        // Given
        final String mode = "v2";
        ReflectionTestUtils.setField(handlerFactory, "mode", mode);
        when(adminTemplate.tableExists(HBaseTables.AGENT_STAT_VER2)).thenReturn(false);
        // When
        handlerFactory.getObject();
        // Then
        Assert.fail("Should have thrown IllegalStateException.");
    }

    @Test
    public void dualWriteMode_bothTablesExist() throws Exception {
        // Given
        final Class<? extends Handler> expectedHandlerClass = DualAgentStatHandler.class;
        final String mode = "dualWrite";
        ReflectionTestUtils.setField(handlerFactory, "mode", mode);
        when(adminTemplate.tableExists(HBaseTables.AGENT_STAT)).thenReturn(true);
        when(adminTemplate.tableExists(HBaseTables.AGENT_STAT_VER2)).thenReturn(true);
        // When
        Handler actualHandler = handlerFactory.getObject();
        // Then
        Assert.assertEquals(expectedHandlerClass, actualHandler.getClass());
    }

    @Test(expected = IllegalStateException.class)
    public void dualWriteMode_v1TableDoesNotExist() throws Exception {
        // Given
        final String mode = "dualWrite";
        ReflectionTestUtils.setField(handlerFactory, "mode", mode);
        when(adminTemplate.tableExists(HBaseTables.AGENT_STAT)).thenReturn(false);
        when(adminTemplate.tableExists(HBaseTables.AGENT_STAT_VER2)).thenReturn(true);
        // When
        handlerFactory.getObject();
        // Then
        Assert.fail("Should have thrown IllegalStateException.");
    }

    @Test(expected = IllegalStateException.class)
    public void dualWriteMode_v2TableDoesNotExist() throws Exception {
        // Given
        final String mode = "dualWrite";
        ReflectionTestUtils.setField(handlerFactory, "mode", mode);
        when(adminTemplate.tableExists(HBaseTables.AGENT_STAT)).thenReturn(true);
        when(adminTemplate.tableExists(HBaseTables.AGENT_STAT_VER2)).thenReturn(false);
        // When
        handlerFactory.getObject();
        // Then
        Assert.fail("Should have thrown IllegalStateException.");
    }

    @Test(expected = IllegalStateException.class)
    public void dualWriteMode_bothTablesDoNotExist() throws Exception {
        // Given
        final String mode = "dualWrite";
        ReflectionTestUtils.setField(handlerFactory, "mode", mode);
        when(adminTemplate.tableExists(HBaseTables.AGENT_STAT)).thenReturn(false);
        when(adminTemplate.tableExists(HBaseTables.AGENT_STAT_VER2)).thenReturn(false);
        // When
        handlerFactory.getObject();
        // Then
        Assert.fail("Should have thrown IllegalStateException.");
    }

}

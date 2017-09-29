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

package com.navercorp.pinpoint.web.dao.hbase;

import com.navercorp.pinpoint.common.hbase.HBaseAdminTemplate;
import com.navercorp.pinpoint.common.hbase.HBaseTables;
import com.navercorp.pinpoint.web.dao.TraceDao;
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
public class HbaseTraceDaoFactoryTest {

    @Mock
    private TraceDao v1;

    @Mock
    private TraceDao v2;

    @Mock
    private HBaseAdminTemplate adminTemplate;

    @InjectMocks
    private final HbaseTraceDaoFactory traceDaoFactory = new HbaseTraceDaoFactory();

    @Test
    public void v2Mode_v2TableExists() throws Exception {
        // Given
        final TraceDao expectedDao = v2;
        final String mode = "v2";
        ReflectionTestUtils.setField(traceDaoFactory, "mode", mode);
        when(adminTemplate.tableExists(HBaseTables.TRACE_V2)).thenReturn(true);
        // When
        TraceDao actualDao = traceDaoFactory.getObject();
        // Then
        Assert.assertEquals(expectedDao, actualDao);
    }

    @Test(expected = IllegalStateException.class)
    public void v2Mode_v2TableDoesNotExist() throws Exception {
        // Given
        final String mode = "v2";
        ReflectionTestUtils.setField(traceDaoFactory, "mode", mode);
        when(adminTemplate.tableExists(HBaseTables.TRACE_V2)).thenReturn(false);
        // When
        traceDaoFactory.getObject();
        // Then
        Assert.fail("Should have thrown IllegalStateException.");
    }

    @Test
    public void compatibilityMode_bothTablesExist() throws Exception {
        // Given
        final Class<? extends TraceDao> expectedTraceDaoClass = HbaseTraceCompatibilityDao.class;
        final String mode = "compatibilityMode";
        ReflectionTestUtils.setField(traceDaoFactory, "mode", mode);
        when(adminTemplate.tableExists(HBaseTables.TRACES)).thenReturn(true);
        when(adminTemplate.tableExists(HBaseTables.TRACE_V2)).thenReturn(true);
        // When
        TraceDao actualDao = traceDaoFactory.getObject();
        // Then
        Assert.assertEquals(expectedTraceDaoClass, actualDao.getClass());
    }

    @Test(expected = IllegalStateException.class)
    public void compatibilityMode_v1TableDoesNotExist() throws Exception {
        // Given
        final String mode = "compatibilityMode";
        ReflectionTestUtils.setField(traceDaoFactory, "mode", mode);
        when(adminTemplate.tableExists(HBaseTables.TRACES)).thenReturn(false);
        when(adminTemplate.tableExists(HBaseTables.TRACE_V2)).thenReturn(true);
        // When
        traceDaoFactory.getObject();
        // Then
        Assert.fail("Should have thrown IllegalStateException.");
    }

    @Test(expected = IllegalStateException.class)
    public void compatibilityMode_v2TableDoesNotExist() throws Exception {
        // Given
        final String mode = "compatibilityMode";
        ReflectionTestUtils.setField(traceDaoFactory, "mode", mode);
        when(adminTemplate.tableExists(HBaseTables.TRACES)).thenReturn(true);
        when(adminTemplate.tableExists(HBaseTables.TRACE_V2)).thenReturn(false);
        // When
        traceDaoFactory.getObject();
        // Then
        Assert.fail("Should have thrown IllegalStateException.");
    }

    @Test(expected = IllegalStateException.class)
    public void compatibilityMode_bothTablesDoNotExist() throws Exception {
        // Given
        final String mode = "compatibilityMode";
        ReflectionTestUtils.setField(traceDaoFactory, "mode", mode);
        when(adminTemplate.tableExists(HBaseTables.TRACES)).thenReturn(false);
        when(adminTemplate.tableExists(HBaseTables.TRACE_V2)).thenReturn(false);
        // When
        traceDaoFactory.getObject();
        // Then
        Assert.fail("Should have thrown IllegalStateException.");
    }

    @Test
    public void dualReadMode_bothTablesExist() throws Exception {
        // Given
        final Class<? extends TraceDao> expectedTraceDaoClass = HbaseDualReadDao.class;
        final String mode = "dualRead";
        ReflectionTestUtils.setField(traceDaoFactory, "mode", mode);
        when(adminTemplate.tableExists(HBaseTables.TRACES)).thenReturn(true);
        when(adminTemplate.tableExists(HBaseTables.TRACE_V2)).thenReturn(true);
        // When
        TraceDao actualDao = traceDaoFactory.getObject();
        // Then
        Assert.assertEquals(expectedTraceDaoClass, actualDao.getClass());
    }

    @Test(expected = IllegalStateException.class)
    public void dualReadMode_v1TableDoesNotExist() throws Exception {
        // Given
        final String mode = "dualRead";
        ReflectionTestUtils.setField(traceDaoFactory, "mode", mode);
        when(adminTemplate.tableExists(HBaseTables.TRACES)).thenReturn(false);
        when(adminTemplate.tableExists(HBaseTables.TRACE_V2)).thenReturn(true);
        // When
        traceDaoFactory.getObject();
        // Then
        Assert.fail("Should have thrown IllegalStateException.");
    }

    @Test(expected = IllegalStateException.class)
    public void dualReadMode_v2TableDoesNotExist() throws Exception {
        // Given
        final String mode = "dualRead";
        ReflectionTestUtils.setField(traceDaoFactory, "mode", mode);
        when(adminTemplate.tableExists(HBaseTables.TRACES)).thenReturn(true);
        when(adminTemplate.tableExists(HBaseTables.TRACE_V2)).thenReturn(false);
        // When
        traceDaoFactory.getObject();
        // Then
        Assert.fail("Should have thrown IllegalStateException.");
    }

    @Test(expected = IllegalStateException.class)
    public void dualReadMode_bothTablesDoNotExist() throws Exception {
        // Given
        final String mode = "dualRead";
        ReflectionTestUtils.setField(traceDaoFactory, "mode", mode);
        when(adminTemplate.tableExists(HBaseTables.TRACES)).thenReturn(false);
        when(adminTemplate.tableExists(HBaseTables.TRACE_V2)).thenReturn(false);
        // When
        traceDaoFactory.getObject();
        // Then
        Assert.fail("Should have thrown IllegalStateException.");
    }
}

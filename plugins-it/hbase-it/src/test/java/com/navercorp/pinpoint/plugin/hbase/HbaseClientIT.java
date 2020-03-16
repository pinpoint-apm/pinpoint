/*
 * Copyright 2019 NAVER Corp.
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

package com.navercorp.pinpoint.plugin.hbase;

import com.navercorp.pinpoint.bootstrap.plugin.test.PluginTestVerifier;
import com.navercorp.pinpoint.bootstrap.plugin.test.PluginTestVerifierHolder;
import com.navercorp.pinpoint.pluginit.utils.AgentPath;
import com.navercorp.pinpoint.test.plugin.*;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.*;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static com.navercorp.pinpoint.bootstrap.plugin.test.Expectations.annotation;
import static com.navercorp.pinpoint.bootstrap.plugin.test.Expectations.event;
import static org.mockito.Mockito.doReturn;

@RunWith(PinpointPluginTestSuite.class)
@PinpointAgent(AgentPath.PATH)
@JvmVersion(8)
@Dependency({"org.apache.hbase:hbase-shaded-client:[1.2.6.1]", "org.mockito:mockito-core:2.7.22"})
@ImportPlugin("com.navercorp.pinpoint:pinpoint-hbase-plugin")
@PinpointConfig("hbase/pinpoint-hbase-test.config")
public class HbaseClientIT {

    @Mock
    private ClusterConnection connection;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testAdmin() throws Exception {

        doReturn(new Configuration()).when(connection).getConfiguration();

        Admin admin = new HBaseAdmin(connection);

        try {
            admin.tableExists(TableName.valueOf("test"));
        } catch (Exception ignore) {
            //
        }

        PluginTestVerifier verifier = PluginTestVerifierHolder.getInstance();

        verifier.printCache();

        verifier.verifyTrace(event("HBASE_CLIENT_ADMIN", HBaseAdmin.class.getDeclaredMethod("tableExists", TableName.class),
                annotation("hbase.client.params", "[test]")));

        verifier.verifyTraceCount(0);
    }

    @Test
    public void testTable() throws Exception {

        doReturn(new Configuration()).when(connection).getConfiguration();

        Table table = new HTable(TableName.valueOf("test"), connection);

        Put put = new Put("row".getBytes());

        try {
            table.put(put);
        } catch (Exception ignore) {
            //
        }

        PluginTestVerifier verifier = PluginTestVerifierHolder.getInstance();

        verifier.printCache();

        verifier.verifyTrace(event("HBASE_CLIENT_TABLE", HTable.class.getDeclaredMethod("put", Put.class),
                annotation("hbase.client.params", "rowKey: row")));

        verifier.verifyTraceCount(0);
    }
}

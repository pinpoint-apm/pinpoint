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
import com.navercorp.pinpoint.test.plugin.Dependency;
import com.navercorp.pinpoint.test.plugin.ImportPlugin;
import com.navercorp.pinpoint.test.plugin.JvmVersion;
import com.navercorp.pinpoint.test.plugin.PinpointAgent;
import com.navercorp.pinpoint.test.plugin.PinpointConfig;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.Admin;
import org.apache.hadoop.hbase.client.BufferedMutatorImpl;
import org.apache.hadoop.hbase.client.BufferedMutatorParams;
import org.apache.hadoop.hbase.client.ClusterConnection;
import org.apache.hadoop.hbase.client.HBaseAdmin;
import org.apache.hadoop.hbase.client.HTable;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.client.Table;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static com.navercorp.pinpoint.bootstrap.plugin.test.Expectations.annotation;
import static com.navercorp.pinpoint.bootstrap.plugin.test.Expectations.event;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

@PinpointAgent(AgentPath.PATH)
@JvmVersion(8)
@Dependency({"org.apache.hbase:hbase-shaded-client:[1.2.6.1]", "org.mockito:mockito-core:4.8.1"})
@ImportPlugin("com.navercorp.pinpoint:pinpoint-hbase-plugin")
@PinpointConfig("hbase/pinpoint-hbase-test.config")
public class HbaseClientIT {

    @Mock
    private ClusterConnection connection;

    private final String tableName = "Table";

    private AutoCloseable openMocks;
    
    @BeforeEach
    public void beforeEach() {
        openMocks = MockitoAnnotations.openMocks(this);
    }

    @AfterEach
    public void after() throws Exception {
        openMocks.close();
    }

    @Test
    public void testAdmin() throws Exception {

        doReturn(new Configuration()).when(connection).getConfiguration();

        Admin admin = new HBaseAdmin(connection);

        try {
            admin.tableExists(TableName.valueOf(tableName));
        } catch (Exception ignored) {
            //
        }

        PluginTestVerifier verifier = PluginTestVerifierHolder.getInstance();

        verifier.printMethod();

        verifier.verifyTrace(event("HBASE_CLIENT_ADMIN", HBaseAdmin.class.getDeclaredMethod("tableExists", TableName.class),
                annotation("hbase.client.params", String.format("[%s]", tableName))));

        verifier.verifyTraceCount(0);
    }

    @Test
    public void testTable() throws Exception {

        Configuration configuration = new Configuration();
        doReturn(configuration).when(connection).getConfiguration();
        doReturn(mock(BufferedMutatorImpl.class)).when(connection).getBufferedMutator(any(BufferedMutatorParams.class));

        Table table = new HTable(TableName.valueOf(tableName), connection);

        Put put = new Put("row".getBytes());

        table.put(put);

        PluginTestVerifier verifier = PluginTestVerifierHolder.getInstance();

        verifier.printMethod();

        verifier.verifyTrace(event("HBASE_CLIENT_TABLE", HTable.class.getDeclaredMethod("put", Put.class),
                annotation("hbase.client.params", "rowKey: row"),
                annotation("hbase.table.name", tableName)));

        verifier.verifyTraceCount(0);
    }
}

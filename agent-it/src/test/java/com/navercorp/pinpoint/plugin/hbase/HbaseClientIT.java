package com.navercorp.pinpoint.plugin.hbase;

import com.navercorp.pinpoint.bootstrap.plugin.test.PluginTestVerifier;
import com.navercorp.pinpoint.bootstrap.plugin.test.PluginTestVerifierHolder;
import com.navercorp.pinpoint.plugin.AgentPath;
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
        } catch (Exception e) {
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
        } catch (Exception e) {
        }

        PluginTestVerifier verifier = PluginTestVerifierHolder.getInstance();

        verifier.printCache();

        verifier.verifyTrace(event("HBASE_CLIENT_TABLE", HTable.class.getDeclaredMethod("put", Put.class),
                annotation("hbase.client.params", "rowKey: row")));

        verifier.verifyTraceCount(0);
    }
}

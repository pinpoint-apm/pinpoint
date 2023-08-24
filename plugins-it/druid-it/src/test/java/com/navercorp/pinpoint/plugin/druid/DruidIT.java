package com.navercorp.pinpoint.plugin.druid;

import com.alibaba.druid.pool.DruidDataSource;
import com.alibaba.druid.pool.DruidPooledConnection;
import com.navercorp.pinpoint.bootstrap.plugin.test.PluginTestVerifier;
import com.navercorp.pinpoint.bootstrap.plugin.test.PluginTestVerifierHolder;
import com.navercorp.pinpoint.pluginit.utils.AgentPath;
import com.navercorp.pinpoint.test.plugin.Dependency;
import com.navercorp.pinpoint.test.plugin.ImportPlugin;
import com.navercorp.pinpoint.test.plugin.JvmVersion;
import com.navercorp.pinpoint.test.plugin.PinpointAgent;
import com.navercorp.pinpoint.test.plugin.PinpointConfig;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.SQLException;

import static com.navercorp.pinpoint.bootstrap.plugin.test.Expectations.event;

@PinpointAgent(AgentPath.PATH)
@Dependency({"com.alibaba:druid:[1.0.0][1.0.31],[1.1.0,1.1.20]", "com.h2database:h2:1.4.191"})
@JvmVersion(8)
@ImportPlugin("com.navercorp.pinpoint:pinpoint-druid-plugin")
@PinpointConfig("druid/pinpoint-druid-test.config")
public class DruidIT {
    private static final String serviceType = "DRUID";
    private static final String JDBC_URL = "jdbc:h2:mem:test;DB_CLOSE_DELAY=-1";

    private static Method getConnectionMethod;
    private static Method closeConnectionMethod;

    @BeforeAll
    public static void setUp() throws NoSuchMethodException {
        getConnectionMethod = DruidDataSource.class.getDeclaredMethod("getConnection");
        closeConnectionMethod = DruidPooledConnection.class.getDeclaredMethod("close");
    }

    @Test
    public void test() throws InterruptedException, SQLException {
        DruidDataSource dataSource = new DruidDataSource();

        dataSource.setUrl(JDBC_URL);
        dataSource.setValidationQuery("select 'x'");
        dataSource.setUsername("test");
        dataSource.setPassword("test");

        dataSource.init();
        try {
            Connection connection = dataSource.getConnection();
            Assertions.assertNotNull(connection);

            connection.close();

            PluginTestVerifier verifier = PluginTestVerifierHolder.getInstance();
            verifier.printCache();

            verifier.verifyTrace(event(serviceType, getConnectionMethod));
            verifier.verifyTrace(event(serviceType, closeConnectionMethod));
        } finally {
            if (dataSource != null) {
                dataSource.close();
            }
        }
    }

}
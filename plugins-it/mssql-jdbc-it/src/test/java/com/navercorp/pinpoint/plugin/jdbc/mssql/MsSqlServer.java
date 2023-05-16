package com.navercorp.pinpoint.plugin.jdbc.mssql;

import com.navercorp.pinpoint.pluginit.jdbc.testcontainers.DatabaseContainers;
import com.navercorp.pinpoint.test.plugin.shared.SharedTestLifeCycle;
import org.junit.Assume;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testcontainers.DockerClientFactory;
import org.testcontainers.containers.MSSQLServerContainer;

import java.util.Properties;

public class MsSqlServer implements SharedTestLifeCycle {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    private MSSQLServerContainer mssqlserver;

    @Override
    public Properties beforeAll() {
        Assume.assumeTrue("Docker not enabled", DockerClientFactory.instance().isDockerAvailable());

        mssqlserver = MSSQLServerContainerFactory.newMSSQLServerContainer(logger.getName());
        mssqlserver.start();

        return DatabaseContainers.toProperties(mssqlserver);
    }

    @Override
    public void afterAll() {
        if (mssqlserver != null) {
            mssqlserver.stop();
        }
    }
}

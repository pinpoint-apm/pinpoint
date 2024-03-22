package com.navercorp.pinpoint.it.plugin.jdbc.mssql;

import com.navercorp.pinpoint.it.plugin.utils.jdbc.testcontainers.DatabaseContainers;
import com.navercorp.pinpoint.test.plugin.shared.SharedTestLifeCycle;
import org.junit.jupiter.api.Assumptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testcontainers.DockerClientFactory;
import org.testcontainers.containers.MSSQLServerContainer;

import java.util.Properties;

public class MsSqlServer implements SharedTestLifeCycle {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    private MSSQLServerContainer<?> mssqlserver;

    @Override
    public Properties beforeAll() {
        Assumptions.assumeTrue(DockerClientFactory.instance().isDockerAvailable(), "Docker not enabled");

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

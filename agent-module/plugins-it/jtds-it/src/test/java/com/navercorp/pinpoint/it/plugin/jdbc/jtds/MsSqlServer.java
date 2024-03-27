package com.navercorp.pinpoint.it.plugin.jdbc.jtds;

import com.navercorp.pinpoint.it.plugin.utils.LogOutputStream;
import com.navercorp.pinpoint.it.plugin.utils.jdbc.testcontainers.DatabaseContainers;
import com.navercorp.pinpoint.test.plugin.shared.SharedTestLifeCycle;
import org.junit.Assume;
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
        Assume.assumeTrue("Docker not enabled", DockerClientFactory.instance().isDockerAvailable());

        mssqlserver = new MSSQLServerContainer<>("mcr.microsoft.com/mssql/server:2019-latest");
        mssqlserver.acceptLicense();
        mssqlserver.withInitScript("sql/init_mssql.sql");
        mssqlserver.withPassword(JtdsITConstants.PASSWORD);


        mssqlserver.withLogConsumer(new LogOutputStream(logger::info));
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

package com.navercorp.pinpoint.plugin.jdbc.jtds;

import com.navercorp.pinpoint.pluginit.jdbc.testcontainers.DatabaseContainers;
import com.navercorp.pinpoint.pluginit.utils.LogUtils;
import com.navercorp.pinpoint.test.plugin.shared.SharedTestLifeCycle;
import org.junit.Assume;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testcontainers.DockerClientFactory;
import org.testcontainers.containers.MSSQLServerContainer;
import org.testcontainers.containers.output.OutputFrame;

import java.util.Properties;
import java.util.function.Consumer;

public class MsSqlServer implements SharedTestLifeCycle {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    private MSSQLServerContainer mssqlserver;
    @Override
    public Properties beforeAll() {
        Assume.assumeTrue("Docker not enabled", DockerClientFactory.instance().isDockerAvailable());

        mssqlserver = new MSSQLServerContainer("mcr.microsoft.com/mssql/server:2019-latest");
        mssqlserver.addEnv("ACCEPT_EULA", "y");
        mssqlserver.withInitScript("sql/init_mssql.sql");
        mssqlserver.withPassword(JtdsITConstants.PASSWORD);


        mssqlserver.withLogConsumer(new Consumer<OutputFrame>() {
            @Override
            public void accept(OutputFrame outputFrame) {
                logger.info(LogUtils.removeLineBreak(outputFrame.getUtf8String()));
            }
        });
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

package com.navercorp.pinpoint.plugin.jdbc.mysql;

import com.navercorp.pinpoint.pluginit.jdbc.testcontainers.DatabaseContainers;
import com.navercorp.pinpoint.test.plugin.shared.SharedTestLifeCycle;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Assume;
import org.testcontainers.DockerClientFactory;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.containers.output.OutputFrame;

import java.util.Properties;
import java.util.function.Consumer;

public class MySqlServer implements SharedTestLifeCycle {
    private final Logger logger = LogManager.getLogger(getClass());

    public static final String DATABASE_NAME = "test";
    public static final String USERNAME = "root";
    public static final String PASSWORD = "";

    private MySQLContainer mysqlDB = new MySQLContainer();

    @Override
    public Properties beforeAll() {
        Assume.assumeTrue("Docker not enabled", DockerClientFactory.instance().isDockerAvailable());

        mysqlDB = new MySQLContainer();
        mysqlDB.withLogConsumer(new Consumer<OutputFrame>() {
            @Override
            public void accept(OutputFrame outputFrame) {
                logger.info(outputFrame.getUtf8String());
            }
        });
        mysqlDB.withDatabaseName(DATABASE_NAME);
        mysqlDB.withUsername(USERNAME);
        mysqlDB.withPassword(PASSWORD);
        mysqlDB.withInitScript("init.sql");
//            mysqlDB.
        mysqlDB.withUrlParam("serverTimezone", "UTC");
        mysqlDB.withUrlParam("useSSL", "false");
        mysqlDB.start();

        return DatabaseContainers.toProperties(mysqlDB);
    }

    @Override
    public void afterAll() {
        if (mysqlDB != null) {
            mysqlDB.stop();
        }
    }
}

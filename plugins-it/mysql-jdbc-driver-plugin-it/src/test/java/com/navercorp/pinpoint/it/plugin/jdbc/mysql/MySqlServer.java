package com.navercorp.pinpoint.it.plugin.jdbc.mysql;

import com.navercorp.pinpoint.it.plugin.utils.jdbc.testcontainers.DatabaseContainers;
import com.navercorp.pinpoint.it.plugin.utils.LogUtils;
import com.navercorp.pinpoint.test.plugin.shared.SharedTestLifeCycle;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.Assumptions;
import org.testcontainers.DockerClientFactory;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.containers.output.OutputFrame;
import org.testcontainers.containers.wait.strategy.Wait;

import java.util.Properties;
import java.util.function.Consumer;

public class MySqlServer implements SharedTestLifeCycle {
    private final Logger logger = LogManager.getLogger(getClass());

    public static final String DATABASE_NAME = "test";
    public static final String USERNAME = "root";
    public static final String PASSWORD = "";

    private MySQLContainer mysqlDB = new MySQLContainer("mysql:5.7.34");

    @Override
    public Properties beforeAll() {
        Assumptions.assumeTrue(DockerClientFactory.instance().isDockerAvailable(), "Docker not enabled");

        mysqlDB = new MySQLContainer("mysql:5.7.34");
        mysqlDB.withLogConsumer(new Consumer<OutputFrame>() {
            @Override
            public void accept(OutputFrame outputFrame) {
                logger.info(LogUtils.removeLineBreak(outputFrame.getUtf8String()));
            }
        });
        mysqlDB.waitingFor(Wait.forListeningPort());
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

package com.navercorp.pinpoint.it.plugin.jdbc.mysql;

import com.navercorp.pinpoint.it.plugin.utils.LogOutputStream;
import com.navercorp.pinpoint.it.plugin.utils.PullImage;
import com.navercorp.pinpoint.it.plugin.utils.jdbc.testcontainers.DatabaseContainers;
import com.navercorp.pinpoint.test.plugin.shared.SharedTestLifeCycle;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.Assumptions;
import org.testcontainers.DockerClientFactory;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.mysql.MySQLContainer;

import java.util.Objects;
import java.util.Properties;

public class MySqlServer implements SharedTestLifeCycle {
    private final Logger logger = LogManager.getLogger(getClass());

    public static final String DATABASE_NAME = "test";
    public static final String USERNAME = "root";
    public static final String PASSWORD = "";
    private final String dockerImageName;
    private final boolean forceAmd64;

    private MySQLContainer mysqlDB;

    public MySqlServer(String dockerImageName, boolean forceAmd64) {
        this.dockerImageName = Objects.requireNonNull(dockerImageName, "dockerImageName");
        this.forceAmd64 = forceAmd64;
    }

    @Override
    public Properties beforeAll() {
        Assumptions.assumeTrue(DockerClientFactory.instance().isDockerAvailable(), "Docker not enabled");
        if (forceAmd64) {
            PullImage.pullWithPlatform(dockerImageName, "linux/amd64");
        }

        mysqlDB = new MySQLContainer(dockerImageName);
        mysqlDB.withLogConsumer(new LogOutputStream(logger::info));
        mysqlDB.waitingFor(Wait.forListeningPort());
        mysqlDB.withDatabaseName(DATABASE_NAME);
        mysqlDB.withUsername(USERNAME);
        mysqlDB.withPassword(PASSWORD);
        mysqlDB.withInitScript("init.sql");
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

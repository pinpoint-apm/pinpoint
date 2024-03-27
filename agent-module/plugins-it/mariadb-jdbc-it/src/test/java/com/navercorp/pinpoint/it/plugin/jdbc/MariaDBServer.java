package com.navercorp.pinpoint.it.plugin.jdbc;

import com.navercorp.pinpoint.it.plugin.utils.LogOutputStream;
import com.navercorp.pinpoint.it.plugin.utils.jdbc.testcontainers.DatabaseContainers;
import com.navercorp.pinpoint.test.plugin.shared.SharedTestLifeCycle;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.Assumptions;
import org.testcontainers.DockerClientFactory;
import org.testcontainers.containers.MariaDBContainer;
import org.testcontainers.containers.wait.strategy.Wait;

import java.util.Properties;

public class MariaDBServer implements SharedTestLifeCycle {
    private final Logger logger = LogManager.getLogger(getClass());
    private MariaDBContainer<?> mariaDB;

    public static final String DATABASE_NAME = "test";
    public static final String USERNAME = "root";
    public static final String PASSWORD = "";

    @Override
    public Properties beforeAll() {
        Assumptions.assumeTrue(DockerClientFactory.instance().isDockerAvailable(), "Docker not enabled");

        mariaDB = new MariaDBContainer<>("mariadb:10.6.17");
        mariaDB.withLogConsumer(new LogOutputStream(logger::info));
        mariaDB.waitingFor(Wait.forListeningPort());
        mariaDB.withDatabaseName(DATABASE_NAME);
        mariaDB.withUsername(USERNAME);
        mariaDB.withPassword(PASSWORD);
        mariaDB.withInitScript("jdbc/mariadb/init.sql");
//        mariaDB.withUrlParam("noAccessToProcedureBodies", "true");
        mariaDB.start();

        int port = mariaDB.getMappedPort(3306);
        Properties properties = DatabaseContainers.toProperties(mariaDB);
        properties.setProperty("URL", mariaDB.getHost() + ":" + port);
        System.setProperty("URL", mariaDB.getHost() + ":" + port);

        return properties;
    }

    @Override
    public void afterAll() {
        if (mariaDB != null) {
            mariaDB.stop();
        }
    }
}

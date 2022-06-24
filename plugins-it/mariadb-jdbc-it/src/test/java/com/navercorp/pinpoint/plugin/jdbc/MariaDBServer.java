package com.navercorp.pinpoint.plugin.jdbc;

import com.navercorp.pinpoint.pluginit.jdbc.testcontainers.DatabaseContainers;
import com.navercorp.pinpoint.test.plugin.shared.SharedTestLifeCycle;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Assume;
import org.testcontainers.DockerClientFactory;
import org.testcontainers.containers.MariaDBContainer;
import org.testcontainers.containers.output.OutputFrame;

import java.util.Properties;
import java.util.function.Consumer;

public class MariaDBServer implements SharedTestLifeCycle {
    private final Logger logger = LogManager.getLogger(getClass());
    private MariaDBContainer mariaDB;

    public static final String DATABASE_NAME = "test";
    public static final String USERNAME = "root";
    public static final String PASSWORD = "";

    @Override
    public Properties beforeAll() {
        Assume.assumeTrue("Docker not enabled", DockerClientFactory.instance().isDockerAvailable());

        mariaDB = new MariaDBContainer();
        mariaDB.withLogConsumer(new Consumer<OutputFrame>() {
            @Override
            public void accept(OutputFrame outputFrame) {
                logger.info(outputFrame.getUtf8String());
            }
        });
        mariaDB.withDatabaseName(DATABASE_NAME);
        mariaDB.withUsername(USERNAME);
        mariaDB.withPassword(PASSWORD);
        mariaDB.withInitScript("jdbc/mariadb/init.sql");
//        mariaDB.withUrlParam("noAccessToProcedureBodies", "true");
        mariaDB.start();

        int port = mariaDB.getMappedPort(3306);
        Properties properties = DatabaseContainers.toProperties(mariaDB);
        properties.setProperty("URL", mariaDB.getHost() + ":" + port);
        return properties;
    }

    @Override
    public void afterAll() {
        if (mariaDB != null) {
            mariaDB.stop();
        }
    }
}

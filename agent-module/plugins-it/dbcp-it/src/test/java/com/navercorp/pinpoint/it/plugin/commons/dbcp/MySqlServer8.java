package com.navercorp.pinpoint.it.plugin.commons.dbcp;

import com.navercorp.pinpoint.it.plugin.utils.LogOutputStream;
import com.navercorp.pinpoint.it.plugin.utils.jdbc.testcontainers.DatabaseContainers;
import com.navercorp.pinpoint.test.plugin.shared.SharedTestLifeCycle;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.Assumptions;
import org.testcontainers.DockerClientFactory;
import org.testcontainers.containers.MySQLContainer;

import java.util.Properties;

public class MySqlServer8 implements SharedTestLifeCycle {

    private final Logger logger = LogManager.getLogger(getClass());

    private MySQLContainer<?> mysqlDB;

    public MySqlServer8() {
    }

    @Override
    public Properties beforeAll() {
        Assumptions.assumeTrue(DockerClientFactory.instance().isDockerAvailable(), "Docker not enabled");

        mysqlDB = new MySQLContainer<>("mysql:8.0.36");
        mysqlDB.withLogConsumer(new LogOutputStream(logger::info));

        mysqlDB.start();

        Properties properties = System.getProperties();
        properties.setProperty("mysql.driverClassName", mysqlDB.getDriverClassName());
        return DatabaseContainers.toProperties(mysqlDB);
    }

    @Override
    public void afterAll() {
        if (mysqlDB != null) {
            mysqlDB.stop();
        }
    }
}

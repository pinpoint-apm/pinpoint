package com.navercorp.pinpoint.plugin.jdbc.oracle;

import com.navercorp.pinpoint.pluginit.jdbc.DriverProperties;
import com.navercorp.pinpoint.pluginit.jdbc.JDBCDriverClass;
import com.navercorp.pinpoint.pluginit.jdbc.testcontainers.DatabaseContainers;
import com.navercorp.pinpoint.test.plugin.shared.SharedTestLifeCycle;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Assume;
import org.testcontainers.DockerClientFactory;
import org.testcontainers.containers.OracleContainer;
import org.testcontainers.containers.wait.strategy.WaitStrategy;

import java.time.Duration;
import java.util.Objects;
import java.util.Properties;

public class OracleServer implements SharedTestLifeCycle {
    private final Logger logger = LogManager.getLogger(getClass());
    private final String dockerImageVersion;
    private final WaitStrategy waitStrategy;

    private OracleContainer oracle;

    public OracleServer(String dockerImageVersion, WaitStrategy waitStrategy) {
        this.dockerImageVersion = Objects.requireNonNull(dockerImageVersion, "dockerImageVersion");
        this.waitStrategy = Objects.requireNonNull(waitStrategy, "waitStrategy");
    }

    @Override
    public Properties beforeAll() {
        Assume.assumeTrue("Docker not enabled", DockerClientFactory.instance().isDockerAvailable());

        logger.info("Setting up oracle db... {}", dockerImageVersion);

        startOracleDB(dockerImageVersion, waitStrategy);
        logger.info("initSchema");
        initSchema();


        return DatabaseContainers.toProperties(oracle);
    }

    private void initSchema() {
        DriverProperties driverProperties = new DriverProperties(oracle.getJdbcUrl(), oracle.getUsername(), oracle.getPassword(), new Properties());
        OracleItHelper helper = new OracleItHelper(driverProperties);
        JDBCDriverClass driverClass = new OracleJDBCDriverClass();
        OracleJDBCApi JDBC_API = new OracleJDBCApi(driverClass);
        try {
            helper.create(JDBC_API);
        } catch (Exception e) {
            throw new RuntimeException("schema init error", e);
        }
    }

    private void startOracleDB(String dockerImageVersion, WaitStrategy waitStrategy) {
        Assume.assumeTrue("Docker not enabled", DockerClientFactory.instance().isDockerAvailable());
        oracle = new OracleContainerWithWait(dockerImageVersion);

        if (waitStrategy != null) {
            oracle.setWaitStrategy(waitStrategy);
            oracle.withStartupTimeout(Duration.ofSeconds(300));
            oracle.addEnv("DBCA_ADDITIONAL_PARAMS", "-initParams sga_target=0M pga_aggreegate_target=0M");
            oracle.withReuse(true);
        }

        oracle.start();
    }

    @Override
    public void afterAll() {
        if (oracle != null) {
            oracle.stop();
        }
    }
}

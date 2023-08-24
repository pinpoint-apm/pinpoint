package com.navercorp.pinpoint.it.plugin.jdbc.oracle;

import com.navercorp.pinpoint.it.plugin.utils.jdbc.testcontainers.DatabaseContainers;
import com.navercorp.pinpoint.test.plugin.shared.SharedTestLifeCycle;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.Assumptions;
import org.testcontainers.DockerClientFactory;
import org.testcontainers.containers.OracleContainer;
import org.testcontainers.containers.wait.strategy.WaitStrategy;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
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
        Assumptions.assumeTrue(DockerClientFactory.instance().isDockerAvailable(), "Docker not enabled");

        logger.info("Setting up oracle db... {}", dockerImageVersion);

        startOracleDB(dockerImageVersion, waitStrategy);
        logger.info("initSchema");
        initSchema();

//        System.setProperty("DB_NAME", oracle.getDatabaseName());
        System.setProperty("DB_ADDRESS", oracle.getHost());

        return DatabaseContainers.toProperties(oracle);
    }

    private void initSchema() {
        try {
            create();
        } catch (Exception e) {
            throw new RuntimeException("schema init error", e);
        }
    }


    public void create() throws Exception {
        final Connection conn = connect();

        conn.setAutoCommit(false);

        Statement statement = conn.createStatement();
        statement.execute("CREATE TABLE test (id INTEGER NOT NULL, name VARCHAR(45) NOT NULL, age INTEGER NOT NULL, CONSTRAINT test_pk PRIMARY KEY (id))");
        statement.execute("CREATE SEQUENCE test_seq");
        statement.execute("CREATE OR REPLACE TRIGGER test_trigger BEFORE INSERT ON test FOR EACH ROW BEGIN SELECT test_seq.nextval INTO :new.id FROM dual; END;");
        statement.execute("CREATE OR REPLACE PROCEDURE concatCharacters(a IN VARCHAR2, b IN VARCHAR2, c OUT VARCHAR2) AS BEGIN c := a || b; END concatCharacters;");
        statement.execute("CREATE OR REPLACE PROCEDURE swapAndGetSum(a IN OUT NUMBER, b IN OUT NUMBER, c OUT NUMBER) IS BEGIN c := a; a := b; b := c; SELECT c + a INTO c FROM DUAL; END swapAndGetSum;");
        statement.close();
        conn.commit();
        conn.close();
    }

    private Connection connect() throws SQLException {
        return DriverManager.getConnection(oracle.getJdbcUrl(), oracle.getUsername(), oracle.getPassword());
    }


    private void startOracleDB(String dockerImageVersion, WaitStrategy waitStrategy) {
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

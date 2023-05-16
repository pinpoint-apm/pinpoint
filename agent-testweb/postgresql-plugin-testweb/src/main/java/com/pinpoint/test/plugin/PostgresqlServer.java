package com.pinpoint.test.plugin;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Component;
import ru.yandex.qatools.embed.postgresql.EmbeddedPostgres;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;

import static ru.yandex.qatools.embed.postgresql.distribution.Version.Main.V9_6;

@Component
public class PostgresqlServer {
    private final Logger logger = LogManager.getLogger(this.getClass());

    public String url;

    private EmbeddedPostgres postgres;

    public PostgresqlServer() {
    }

    public String getUrl() {
        return url;
    }

    @PostConstruct
    public void init() throws Exception {
        logger.info("PostgresqlServer init");
        postgres = new EmbeddedPostgres(V9_6);
        // predefined data directory
        // final EmbeddedPostgres postgres = new EmbeddedPostgres(V9_6, "/path/to/predefined/data/directory");
        url = postgres.start("localhost", 5432, "dbName", "userName", "password");

        try (Connection conn = DriverManager.getConnection(url);
             Statement createStatement = conn.createStatement()) {

            createStatement.execute("CREATE TABLE test (name VARCHAR(45), age int);");
            createStatement.execute("CREATE TABLE member (id INT PRIMARY KEY, name CHAR(20));");
        }
    }


    @PreDestroy
    public void destroy() {
        logger.info("PostgresqlServer destroy");
        if (postgres != null) {
            postgres.close();
        }
    }

}

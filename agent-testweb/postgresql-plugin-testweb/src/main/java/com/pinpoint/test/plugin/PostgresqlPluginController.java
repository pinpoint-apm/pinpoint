/*
 * Copyright 2020 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.pinpoint.test.plugin;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;
import ru.yandex.qatools.embed.postgresql.EmbeddedPostgres;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;

import static ru.yandex.qatools.embed.postgresql.distribution.Version.Main.V9_6;

@RestController
public class PostgresqlPluginController {
    private static EmbeddedPostgres postgres;
    private static String url;

    @PostConstruct
    public static void start() throws Exception {
        // starting Postgres
        postgres = new EmbeddedPostgres(V9_6);
        // predefined data directory
        // final EmbeddedPostgres postgres = new EmbeddedPostgres(V9_6, "/path/to/predefined/data/directory");
        url = postgres.start("localhost", 5432, "dbName", "userName", "password");

        final Connection conn = DriverManager.getConnection(url);
        Statement createStatement = conn.createStatement();
        createStatement.execute("CREATE TABLE test (name VARCHAR(45), age int);");
        createStatement.execute("CREATE TABLE member (id INT PRIMARY KEY, name CHAR(20));");

        createStatement.close();
        conn.close();
    }

    @PreDestroy
    public static void shutdown() throws Exception {
        if (postgres != null) {
            postgres.close();
        }
    }

    @GetMapping("/select")
    public Mono<String> select() throws SQLException {
        final String name = "testUser";
        final int age = 5;

        String insertQuery = "INSERT INTO test (name, age) VALUES (?, ?)";
        String selectQuery = "SELECT * FROM test";
        String deleteQuery = "DELETE FROM test";

        final Connection conn = DriverManager.getConnection(url);

        // performing some assertions
        PreparedStatement insertPreparedStatement = conn.prepareStatement(insertQuery);
        insertPreparedStatement.setString(1, name);
        insertPreparedStatement.setInt(2, age);
        insertPreparedStatement.execute();
        insertPreparedStatement.close();

        Statement selectStatement = conn.createStatement();
        selectStatement.executeQuery(selectQuery);
        selectStatement.close();

        Statement deleteStatement = conn.createStatement();
        deleteStatement.executeUpdate(deleteQuery, Statement.NO_GENERATED_KEYS);
        deleteStatement.close();

        conn.close();

        return Mono.just("OK");
    }
}

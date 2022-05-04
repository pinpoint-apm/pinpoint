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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Objects;

@RestController
public class PostgresqlPluginController {
    private final Logger logger = LogManager.getLogger(this.getClass());

    private final PostgresqlServer postgresqlServer;

    public PostgresqlPluginController(PostgresqlServer postgresqlServer) {
        this.postgresqlServer = Objects.requireNonNull(postgresqlServer, "postgresql");
    }

    @GetMapping("/select")
    public Mono<String> select() throws SQLException {
        final String name = "testUser";
        final int age = 5;

        String insertQuery = "INSERT INTO test (name, age) VALUES (?, ?)";
        String selectQuery = "SELECT * FROM test";
        String deleteQuery = "DELETE FROM test";

        try (Connection conn = DriverManager.getConnection(postgresqlServer.getUrl())) {
            // performing some assertions
            try (PreparedStatement insert = conn.prepareStatement(insertQuery)) {
                insert.setString(1, name);
                insert.setInt(2, age);
                insert.execute();
            }

            try (Statement select = conn.createStatement();
                 ResultSet rs = select.executeQuery(selectQuery)) {
                while (rs.next()) {
                    String rsName = rs.getString(1);
                    int rsAge = rs.getInt(2);
                    logger.info("name:{} age:{}", rsName, rsAge);
                }
            }

            try (Statement delete = conn.createStatement()) {
                delete.executeUpdate(deleteQuery, Statement.NO_GENERATED_KEYS);
            }
        }
        return Mono.just("OK");
    }
}

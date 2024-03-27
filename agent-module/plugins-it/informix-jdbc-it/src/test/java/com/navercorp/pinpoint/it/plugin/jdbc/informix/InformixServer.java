/*
 * Copyright 2023 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.navercorp.pinpoint.it.plugin.jdbc.informix;

import com.navercorp.pinpoint.it.plugin.utils.LogOutputStream;
import com.navercorp.pinpoint.test.plugin.shared.SharedTestLifeCycle;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.Assumptions;
import org.testcontainers.DockerClientFactory;
import org.testcontainers.containers.GenericContainer;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

public class InformixServer implements SharedTestLifeCycle {
    private static final Logger LOGGER = LogManager.getLogger(InformixServer.class);

    private static GenericContainer<?> container = new GenericContainer<>("ibmcom/informix-developer-database:latest");

    @Override
    public Properties beforeAll() {
        Assumptions.assumeTrue(DockerClientFactory.instance().isDockerAvailable(), "Docker not enabled");
        container.withPrivilegedMode(true);
        container.withExposedPorts(9088, 9089, 27017, 27018, 27883);
        container.withEnv("LICENSE", "accept");
        container.withEnv("DB_INIT", "1");
        container.withLogConsumer(new LogOutputStream(LOGGER::info));
        container.start();

        Connection connection = null;
        try {
            connection = createConnection("localhost:" + container.getFirstMappedPort() + "/sysmaster", "informix", "in4mix");
            Statement statement = connection.createStatement();

            List<String> tableQuery = createTableQuery();

            for (String query : tableQuery) {
                statement.execute(query);
            }
        } catch (Exception e) {
            LOGGER.error("Failed to start testcontainer", e);
        } finally {
            if (connection != null) {
                try {
                    connection.close();
                } catch (SQLException ignored) {
                }
            }
        }

        Integer port = container.getFirstMappedPort();
        Properties properties = new Properties();
        properties.setProperty("PORT", port.toString());
        System.setProperty("PORT", port.toString());
        return properties;
    }

    private static Connection createConnection(String url, String username, String password) throws SQLException {
        String connectionUrl = createConnectionUrl(url, username, password);
        return DriverManager.getConnection(connectionUrl);
    }

    private static String createConnectionUrl(String url, String username, String password) {
        StringBuilder urlBuilder = new StringBuilder();
        urlBuilder.append("jdbc:informix-sqli:");
        urlBuilder.append(url);
        urlBuilder.append(":");
        urlBuilder.append("user=").append(username).append(";");
        urlBuilder.append("password=").append(password);
        return urlBuilder.toString();
    }


    private static List<String> createTableQuery() {
        String create1 = "CREATE TABLE member \n" +
                "   (\n" +
                "   id  INT PRIMARY KEY,\n" +
                "   name    CHAR(20)\n" +
                "   );";

        return Arrays.asList(create1);
    }

    @Override
    public void afterAll() {
        container.stop();
    }
}

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

import com.datastax.driver.core.BoundStatement;
import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.PreparedStatement;
import com.datastax.driver.core.Session;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
public class Cassandra3PluginController {
    private static final String TEST_KEYSPACE = "mykeyspace";
    private static final String TEST_TABLE = "mytable";
    private static final String TEST_COL_ID = "id";
    private static final String TEST_COL_VALUE = "value";
    private static final String CQL_INSERT = String.format("INSERT INTO %s (%s, %s) VALUES (?, ?);", Cassandra3PluginTestConstants.TEST_TABLE, TEST_COL_ID, TEST_COL_VALUE);

    private static final String HOST = "localhost";
    private static final int PORT = 55923;

    private final Logger logger = LogManager.getLogger(this.getClass());

    @GetMapping("/init")
    public Mono<String> init() throws Exception {
        Cluster cluster = newCluster();
        Session session = cluster.connect();
        String createKeyspace = String.format("CREATE KEYSPACE IF NOT EXISTS %s WITH replication = " +
                "{'class':'SimpleStrategy','replication_factor':'1'};", Cassandra3PluginTestConstants.TEST_KEYSPACE);
        session.execute(createKeyspace);
        String createTable = String.format("CREATE TABLE %s.%s (id text, value text, PRIMARY KEY(id))",
                Cassandra3PluginTestConstants.TEST_KEYSPACE, Cassandra3PluginTestConstants.TEST_TABLE);
        session.execute(createTable);
        closeSession(session);

        return Mono.just("OK");
    }

    @GetMapping("/select")
    public Mono<String> select() throws Exception {
        Session session = getSession();
        PreparedStatement preparedStatement = session.prepare(CQL_INSERT);
        BoundStatement boundStatement = new BoundStatement(preparedStatement);
        boundStatement.bind("foo", "bar");
        session.execute(boundStatement);
        closeSession(session);

        return Mono.just("OK");
    }

    private Cluster newCluster() {
        Cluster.Builder builder = Cluster.builder();
        builder.addContactPoint(HOST);
        builder.withPort(PORT);
        builder.withoutMetrics();
        return builder.build();
    }

    private Session getSession() {
        Cluster cluster = newCluster();
        return cluster.connect(TEST_KEYSPACE);
    }

    private void closeSession(Session session) {
        if (session != null) {
            session.close();
        }
    }
}

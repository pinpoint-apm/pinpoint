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

import com.datastax.oss.driver.api.core.CqlSession;
import com.datastax.oss.driver.api.core.cql.BoundStatement;
import com.datastax.oss.driver.api.core.cql.PreparedStatement;
import com.datastax.oss.driver.api.core.cql.ResultSet;
import com.datastax.oss.driver.api.core.cql.SimpleStatement;
import com.datastax.oss.driver.api.core.cql.Statement;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.net.InetSocketAddress;

@RestController
public class Cassandra4PluginController {
    private static final String TEST_KEYSPACE = "mykeyspace";
    private static final String TEST_TABLE = "mytable";
    private static final String TEST_COL_ID = "id";
    private static final String TEST_COL_VALUE = "value";
    private static final String CQL_INSERT = String.format("INSERT INTO %s (%s, %s) VALUES (?, ?);", Cassandra4PluginTestConstants.TEST_TABLE, TEST_COL_ID, TEST_COL_VALUE);
    private static final String CQL_SELECT = String.format("SELECT * FROM %s", TEST_TABLE);

    private static final String HOST = "localhost";
    private static final int PORT = 61306;
    private static final String LOCAL_DATACENTER = "datacenter1";

    private final Logger logger = LogManager.getLogger(this.getClass());

    @GetMapping("/init")
    public Mono<String> init() throws Exception {
        CqlSession session = getSession();
        String createKeyspace = String.format("CREATE KEYSPACE IF NOT EXISTS %s WITH replication = " +
                "{'class':'SimpleStrategy','replication_factor':'1'};", Cassandra4PluginTestConstants.TEST_KEYSPACE);
        session.execute(createKeyspace);
        String createTable = String.format("CREATE TABLE %s.%s (id text, value text, PRIMARY KEY(id))",
                Cassandra4PluginTestConstants.TEST_KEYSPACE, Cassandra4PluginTestConstants.TEST_TABLE);
        session.execute(createTable);

        closeSession(session);

        return Mono.just("OK");
    }

    @GetMapping("/insert")
    public Mono<String> insert() throws Exception {
        CqlSession session = CqlSession.builder().addContactPoint(new InetSocketAddress(HOST, PORT)).withLocalDatacenter(LOCAL_DATACENTER).withKeyspace(Cassandra4PluginTestConstants.TEST_KEYSPACE).build();

        // SimpleStatement
        Statement statement = SimpleStatement.builder(CQL_INSERT).addPositionalValue("simple").addPositionalValue("statement").build();
        session.execute(statement);

        // PreparedStatement
        PreparedStatement preparedStatement = session.prepare(CQL_INSERT);
        session.execute(preparedStatement.bind("prepared", "statement"));

        // BoundStatement
        BoundStatement boundStatement = preparedStatement.bind("prepared", "statement");
        boundStatement.set(1, "bound", String.class);
        session.execute(boundStatement);

        closeSession(session);

        return Mono.just("OK");
    }

    @GetMapping("/select")
    public Mono<String> select() throws Exception {
        CqlSession session = CqlSession.builder().addContactPoint(new InetSocketAddress(HOST, PORT)).withLocalDatacenter(LOCAL_DATACENTER).withKeyspace(Cassandra4PluginTestConstants.TEST_KEYSPACE).build();
        ResultSet resultSet = session.execute(CQL_SELECT);

        closeSession(session);

        return Mono.just("OK");
    }

    private CqlSession getSession() {
        CqlSession session = CqlSession.builder().addContactPoint(new InetSocketAddress(HOST, PORT)).withLocalDatacenter(LOCAL_DATACENTER).build();
        return session;
    }

    private void closeSession(CqlSession session) {
        if (session != null) {
            session.close();
        }
    }
}

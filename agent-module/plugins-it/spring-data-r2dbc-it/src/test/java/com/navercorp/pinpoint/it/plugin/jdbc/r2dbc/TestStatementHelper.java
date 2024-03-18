/*
 * Copyright 2022 NAVER Corp.
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

package com.navercorp.pinpoint.it.plugin.jdbc.r2dbc;

import com.navercorp.pinpoint.bootstrap.plugin.test.PluginTestVerifier;
import com.navercorp.pinpoint.bootstrap.plugin.test.PluginTestVerifierHolder;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import org.springframework.r2dbc.core.DatabaseClient;

import java.lang.reflect.Method;
import java.util.function.Supplier;

import static com.navercorp.pinpoint.bootstrap.plugin.test.Expectations.event;

public class TestStatementHelper {

    public void sql(R2dbcEntityTemplate template) throws Exception {
        final DatabaseClient databaseClient = template.getDatabaseClient();
        databaseClient
                .sql("INSERT INTO  persons (first_name, last_name, age) VALUES (:first_name, :last_name, :age)")
                .bind("first_name", "foo")
                .bind("last_name", "bar")
                .bind("age", 25)
                .fetch()
                .all()
                .blockLast();

        PluginTestVerifier verifier = PluginTestVerifierHolder.getInstance();
        verifier.printCache();

        final ClassLoader classLoader = this.getClass().getClassLoader();
        final Class defaultDatabaseClientClass = classLoader.loadClass("org.springframework.r2dbc.core.DefaultDatabaseClient");
        final Method sqlMethod = defaultDatabaseClientClass.getMethod("sql", Supplier.class);
        final String endPoint = null;
        final String destinationId = null;
        verifier.verifyTrace(event("SPRING_DATA_R2DBC", sqlMethod, null, endPoint, destinationId));
    }

}

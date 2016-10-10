/**
 * Copyright 2014 NAVER Corp.
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
package com.navercorp.pinpoint.plugin.jdbc.sqlite;

import static com.navercorp.pinpoint.common.trace.ServiceTypeProperty.INCLUDE_DESTINATION_ID;
import static com.navercorp.pinpoint.common.trace.ServiceTypeProperty.RECORD_STATISTICS;
import static com.navercorp.pinpoint.common.trace.ServiceTypeProperty.TERMINAL;

import com.navercorp.pinpoint.common.trace.ServiceType;
import com.navercorp.pinpoint.common.trace.ServiceTypeFactory;

/**
 * @author barney
 *
 */
public final class SqlitePluginConstants {

    private SqlitePluginConstants() {
    }

    public static final ServiceType SQLITE = ServiceTypeFactory.of(2700, "SQLITE", TERMINAL, INCLUDE_DESTINATION_ID);

    public static final ServiceType SQLITE_EXECUTE_QUERY = ServiceTypeFactory.of(2701, "SQLITE_EXECUTE_QUERY", "SQLITE", TERMINAL, RECORD_STATISTICS, INCLUDE_DESTINATION_ID);

    public static final String SQLITE_SCOPE = "SQLITE_JDBC";
}

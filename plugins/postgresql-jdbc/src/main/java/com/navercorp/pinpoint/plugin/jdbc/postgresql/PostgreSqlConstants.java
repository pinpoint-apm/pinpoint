/*
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
package com.navercorp.pinpoint.plugin.jdbc.postgresql;

import static com.navercorp.pinpoint.common.trace.ServiceTypeProperty.*;

import com.navercorp.pinpoint.common.trace.ServiceType;
import com.navercorp.pinpoint.common.trace.ServiceTypeFactory;

/**
 * @author Brad Hong
 *
 */
public final class PostgreSqlConstants {
    private PostgreSqlConstants() {
    }

    public static final String POSTGRESQL_SCOPE = "POSTGRESQL_JDBC";
    
    public static final ServiceType POSTGRESQL = ServiceTypeFactory.of(2500, "POSTGRESQL", TERMINAL, INCLUDE_DESTINATION_ID);
    public static final ServiceType POSTGRESQL_EXECUTE_QUERY = ServiceTypeFactory.of(2501, "POSTGRESQL_EXECUTE_QUERY", "POSTGRESQL", TERMINAL, RECORD_STATISTICS, INCLUDE_DESTINATION_ID);
}

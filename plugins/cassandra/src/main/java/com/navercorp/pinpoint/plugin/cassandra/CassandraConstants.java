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
package com.navercorp.pinpoint.plugin.cassandra;

import static com.navercorp.pinpoint.common.trace.ServiceTypeProperty.*;

import com.navercorp.pinpoint.common.trace.ServiceType;
import com.navercorp.pinpoint.common.trace.ServiceTypeFactory;

/**
 * @author dawidmalina
 *
 */
public final class CassandraConstants {
    private CassandraConstants() {
    }

    public static final String CASSANDRA_SCOPE = "CASSANDRA_CQL";

    public static final ServiceType CASSANDRA = ServiceTypeFactory.of(2600, "CASSANDRA", TERMINAL, INCLUDE_DESTINATION_ID);
    public static final ServiceType CASSANDRA_EXECUTE_QUERY = ServiceTypeFactory.of(2601, "CASSANDRA_EXECUTE_QUERY",
            "CASSANDRA", TERMINAL, RECORD_STATISTICS, INCLUDE_DESTINATION_ID);
}

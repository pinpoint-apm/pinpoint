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
package com.navercorp.pinpoint.plugin.cassandra4;

import com.navercorp.pinpoint.common.trace.ServiceType;
import com.navercorp.pinpoint.common.trace.ServiceTypeProvider;

public final class CassandraConstants {
    private CassandraConstants() {
    }

    public static final String CASSANDRA_SCOPE = "CASSANDRA_CQL";

    public static final ServiceType CASSANDRA = ServiceTypeProvider.getByName("CASSANDRA4");
    public static final ServiceType CASSANDRA_EXECUTE_QUERY = ServiceTypeProvider.getByName("CASSANDRA4_EXECUTE_QUERY");
}

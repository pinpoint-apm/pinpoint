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
package com.navercorp.pinpoint.plugin.jdbc.oracle;

import static com.navercorp.pinpoint.common.trace.ServiceTypeProperty.*;

import com.navercorp.pinpoint.common.trace.ServiceType;
import com.navercorp.pinpoint.common.trace.ServiceTypeFactory;

/**
 * @author Jongho Moon
 *
 */
public final class OracleConstants {
    private OracleConstants() {
    }

    public static final String ORACLE_SCOPE = "ORACLE_SCOPE";
    
    public static final ServiceType ORACLE = ServiceTypeFactory.of(2300, "ORACLE", TERMINAL, INCLUDE_DESTINATION_ID);
    public static final ServiceType ORACLE_EXECUTE_QUERY = ServiceTypeFactory.of(2301, "ORACLE_EXECUTE_QUERY", "ORACLE", TERMINAL, RECORD_STATISTICS, INCLUDE_DESTINATION_ID);
}

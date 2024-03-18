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
package com.navercorp.pinpoint.plugin.jdbc.cubrid;

import com.navercorp.pinpoint.common.trace.ServiceType;
import com.navercorp.pinpoint.common.trace.ServiceTypeFactory;

import static com.navercorp.pinpoint.common.trace.ServiceTypeProperty.INCLUDE_DESTINATION_ID;
import static com.navercorp.pinpoint.common.trace.ServiceTypeProperty.RECORD_STATISTICS;
import static com.navercorp.pinpoint.common.trace.ServiceTypeProperty.TERMINAL;

/**
 * @author Jongho Moon
 *
 */
public final class CubridConstants {
    private CubridConstants() {
    }

    public static final String CUBRID_SCOPE = "CUBRID_SCOPE";
    
    public static final ServiceType CUBRID = ServiceTypeFactory.of(2400, "CUBRID", TERMINAL, INCLUDE_DESTINATION_ID);
    public static final ServiceType CUBRID_EXECUTE_QUERY = ServiceTypeFactory.of(2401, "CUBRID_EXECUTE_QUERY", "CUBRID", TERMINAL, RECORD_STATISTICS, INCLUDE_DESTINATION_ID);
}

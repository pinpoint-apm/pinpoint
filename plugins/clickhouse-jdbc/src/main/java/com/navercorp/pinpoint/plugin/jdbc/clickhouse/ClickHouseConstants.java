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
package com.navercorp.pinpoint.plugin.jdbc.clickhouse;

import static com.navercorp.pinpoint.common.trace.ServiceTypeProperty.INCLUDE_DESTINATION_ID;
import static com.navercorp.pinpoint.common.trace.ServiceTypeProperty.RECORD_STATISTICS;
import static com.navercorp.pinpoint.common.trace.ServiceTypeProperty.TERMINAL;

import com.navercorp.pinpoint.common.trace.ServiceType;
import com.navercorp.pinpoint.common.trace.ServiceTypeFactory;

/**
 * @author Jongho Moon
 *
 */
public final class ClickHouseConstants {
    private ClickHouseConstants() {
    }

    public static final String CLICK_HOUSE_SCOPE = "CLICK_HOUSE_JDBC";
    
    public static final ServiceType CLICK_HOUSE = ServiceTypeFactory.of(2800, "CLICK_HOUSE", TERMINAL, INCLUDE_DESTINATION_ID);
    public static final ServiceType CLICK_HOUSE_EXECUTE_QUERY = ServiceTypeFactory.of(2801, "CLICK_HOUSE_EXECUTE_QUERY", "CLICK_HOUSE", TERMINAL, RECORD_STATISTICS, INCLUDE_DESTINATION_ID);
}

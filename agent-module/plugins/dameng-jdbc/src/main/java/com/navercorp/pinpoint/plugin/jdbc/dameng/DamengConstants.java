/*
 * Copyright 2024 NAVER Corp.
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
package com.navercorp.pinpoint.plugin.jdbc.dameng;

import com.navercorp.pinpoint.common.trace.ServiceType;
import com.navercorp.pinpoint.common.trace.ServiceTypeFactory;

import static com.navercorp.pinpoint.common.trace.ServiceTypeProperty.INCLUDE_DESTINATION_ID;
import static com.navercorp.pinpoint.common.trace.ServiceTypeProperty.RECORD_STATISTICS;
import static com.navercorp.pinpoint.common.trace.ServiceTypeProperty.TERMINAL;

/**
 * @author yjqg6666
 *
 */
public final class DamengConstants {
    private DamengConstants() {
    }

    public static final String DAMENG_SCOPE = "DAMENG_JDBC";

    public static final ServiceType DAMENG = ServiceTypeFactory.of(2340, "DAMENG", TERMINAL, INCLUDE_DESTINATION_ID);
    public static final ServiceType DAMENG_EXECUTE_QUERY = ServiceTypeFactory.of(2341, "DAMENG_EXECUTE_QUERY",
            "DAMENG", TERMINAL, RECORD_STATISTICS, INCLUDE_DESTINATION_ID);
}

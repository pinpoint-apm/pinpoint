/*
 * Copyright 2017 NAVER Corp.
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

package com.navercorp.pinpoint.plugin.kafka;

import com.navercorp.pinpoint.common.trace.ServiceType;
import com.navercorp.pinpoint.common.trace.ServiceTypeFactory;

import static com.navercorp.pinpoint.common.trace.ServiceTypeProperty.RECORD_STATISTICS;

public class KafkaConstants {
    public static final ServiceType KAFKA = ServiceTypeFactory.of(9995, "KAFKA", "KAFKA", RECORD_STATISTICS);
    public static final String PINPOINT_HEADER_DELIMITIER = "@";
    public static final String PINPOINT_HEADER_PREFIX = PINPOINT_HEADER_DELIMITIER + "pinpoint_start" + PINPOINT_HEADER_DELIMITIER;
    public static final String PINPOINT_HEADER_POSTFIX = PINPOINT_HEADER_DELIMITIER + "pinpoint_end";


}

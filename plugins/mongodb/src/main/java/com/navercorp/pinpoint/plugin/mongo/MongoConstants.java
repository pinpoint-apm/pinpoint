/*
 * Copyright 2018 NAVER Corp.
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
package com.navercorp.pinpoint.plugin.mongo;

import static com.navercorp.pinpoint.common.trace.ServiceTypeProperty.*;

import com.navercorp.pinpoint.common.trace.ServiceType;
import com.navercorp.pinpoint.common.trace.ServiceTypeFactory;

/**
 * @author Roy Kim
 */
public final class MongoConstants {
    private MongoConstants() {
    }

    public static final String MONGO_SCOPE = "MONGO_JAVA_DRIVER";

    public static final ServiceType MONGO = ServiceTypeFactory.of(2650, "MONGODB", TERMINAL, INCLUDE_DESTINATION_ID);
    public static final ServiceType MONGO_EXECUTE_QUERY = ServiceTypeFactory.of(2651, "MONGO_EXECUTE_QUERY", "MONGODB", TERMINAL, RECORD_STATISTICS, INCLUDE_DESTINATION_ID);
}

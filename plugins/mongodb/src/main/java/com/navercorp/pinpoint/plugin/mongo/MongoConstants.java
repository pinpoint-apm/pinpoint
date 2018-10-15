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

import com.navercorp.pinpoint.common.trace.AnnotationKey;
import com.navercorp.pinpoint.common.trace.AnnotationKeyFactory;
import com.navercorp.pinpoint.common.trace.ServiceType;
import com.navercorp.pinpoint.common.trace.ServiceTypeFactory;

import static com.navercorp.pinpoint.common.trace.AnnotationKeyProperty.VIEW_IN_RECORD_SET;
import static com.navercorp.pinpoint.common.trace.ServiceTypeProperty.*;

/**
 * @author Roy Kim
 */
public final class MongoConstants {
    private MongoConstants() {
    }

    public static final String MONGO_SCOPE = "MONGO_JAVA_DRIVER";

    public static final AnnotationKey MONGO_JSON = AnnotationKeyFactory.of(51, "MONGO-JSON");
    public static final AnnotationKey MONGO_COLLECTION_INFO = AnnotationKeyFactory.of(52, "Collection-Info", VIEW_IN_RECORD_SET);
    public static final AnnotationKey MONGO_COLLECTION_OPTION = AnnotationKeyFactory.of(53, "Collection-Option");
    public static final AnnotationKey MONGO_JSON_BINDVALUE = AnnotationKeyFactory.of(54, "MONGO-JSON-BindValue", VIEW_IN_RECORD_SET);

    public static final ServiceType MONGODB = ServiceTypeFactory.of(2650, "MONGO", TERMINAL, INCLUDE_DESTINATION_ID);
    public static final ServiceType MONGO_EXECUTE_QUERY = ServiceTypeFactory.of(2651, "MONGO_EXECUTE_QUERY", "MONGO", TERMINAL, RECORD_STATISTICS, INCLUDE_DESTINATION_ID);
}

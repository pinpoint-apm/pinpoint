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

import java.util.Arrays;
import java.util.List;

import static com.navercorp.pinpoint.common.trace.AnnotationKeyProperty.VIEW_IN_RECORD_SET;
import static com.navercorp.pinpoint.common.trace.ServiceTypeProperty.*;

/**
 * @author Roy Kim
 */
public final class MongoConstants {
    private MongoConstants() {
    }

    static final String MONGO_SCOPE = "MONGO_JAVA_DRIVER";

    static final String MONGO_FILTER_NOT = "com.mongodb.client.model.Filters$NotFilter";
    static final String MONGO_FILTER_SIMPLEENCODING = "com.mongodb.client.model.Filters$SimpleEncodingFilter";
    static final String MONGO_FILTER_ITERABLEOPERATOR = "com.mongodb.client.model.Filters$IterableOperatorFilter";
    static final String MONGO_FILTER_OR = "com.mongodb.client.model.Filters$OrFilter";
    static final String MONGO_FILTER_AND = "com.mongodb.client.model.Filters$AndFilter";
    static final String MONGO_FILTER_OPERATOR = "com.mongodb.client.model.Filters$OperatorFilter";
    static final String MONGO_FILTER_SIMPLE = "com.mongodb.client.model.Filters$SimpleFilter";
    static final String MONGO_FILTER_GEOMETRYOPERATOR = "com.mongodb.client.model.Filters$.GeometryOperatorFilter";
    static final String MONGO_FILTER_TEXT = "com.mongodb.client.model.Filters$TextFilter";
    static final String MONGO_FILTER_ORNOR = "com.mongodb.client.model.Filters$OrNorFilter";

    static final List<String> FILTERLIST = Arrays.asList(
        MONGO_FILTER_NOT,
        MONGO_FILTER_SIMPLEENCODING,
        MONGO_FILTER_ITERABLEOPERATOR,
        MONGO_FILTER_OR,
        MONGO_FILTER_AND,
        MONGO_FILTER_OPERATOR,
        MONGO_FILTER_SIMPLE,
        MONGO_FILTER_GEOMETRYOPERATOR,
        MONGO_FILTER_TEXT,
        MONGO_FILTER_ORNOR
    );

    static final String MONGO_UPDATES_SIMPLE = "com.mongodb.client.model.Updates$SimpleUpdate";
    static final String MONGO_UPDATES_WITHEACH = "com.mongodb.client.model.Updates$WithEachUpdate";
    static final String MONGO_UPDATES_PUSH = "com.mongodb.client.model.Updates$PushUpdate";
    static final String MONGO_UPDATES_PULLALL = "com.mongodb.client.model.Updates$PullAllUpdate";
    static final String MONGO_UPDATES_COMPOSITE = "com.mongodb.client.model.Updates$CompositeUpdate";

    static final List<String> UPDATESLIST = Arrays.asList(
        MONGO_UPDATES_SIMPLE,
        MONGO_UPDATES_WITHEACH,
        MONGO_UPDATES_PUSH,
        MONGO_UPDATES_PULLALL,
        MONGO_UPDATES_COMPOSITE
    );

    static final String MONGO_SORT_COMPOSITE = "com.mongodb.client.model.Sorts$CompoundSort";

    public static final AnnotationKey MONGO_JSON_DATA = AnnotationKeyFactory.of(150, "MONGO-JSON-Data");
    public static final AnnotationKey MONGO_COLLECTION_INFO = AnnotationKeyFactory.of(151, "Collection-Info", VIEW_IN_RECORD_SET);
    public static final AnnotationKey MONGO_COLLECTION_OPTION = AnnotationKeyFactory.of(152, "Collection-Option");
    public static final AnnotationKey MONGO_JSON = AnnotationKeyFactory.of(153, "MONGO-JSON", VIEW_IN_RECORD_SET);
    public static final AnnotationKey MONGO_JSON_BINDVALUE = AnnotationKeyFactory.of(154, "MONGO-JSON-BindValue", VIEW_IN_RECORD_SET);

    public static final ServiceType MONGODB = ServiceTypeFactory.of(2650, "MONGO", TERMINAL, INCLUDE_DESTINATION_ID);
    public static final ServiceType MONGO_EXECUTE_QUERY = ServiceTypeFactory.of(2651, "MONGO_EXECUTE_QUERY", "MONGO", TERMINAL, RECORD_STATISTICS, INCLUDE_DESTINATION_ID);
    public static final ServiceType MONGO_REACTIVE = ServiceTypeFactory.of(2652, "MONGO_REACTIVE", "MONGO");
}

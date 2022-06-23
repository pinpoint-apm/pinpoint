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

import com.mongodb.MongoClientSettings;
import com.mongodb.ServerAddress;
import com.mongodb.WriteConcern;
import com.navercorp.pinpoint.bootstrap.context.SpanEventRecorder;
import com.navercorp.pinpoint.common.plugin.util.HostAndPort;
import com.navercorp.pinpoint.common.util.StringJoinUtils;
import com.navercorp.pinpoint.common.util.StringStringValue;
import org.bson.BsonType;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author Roy Kim
 */
public final class MongoUtil {

    public static final String SEPARATOR = ",";
    private static final MongoWriteConcernMapper mongoWriteConcernMapper = new MongoWriteConcernMapper();

    private static final boolean decimal128Enabled = decimal128Enabled();

    private MongoUtil() {
    }

    //since Mongo Java Driver 3.4
    private static boolean decimal128Enabled() {
        for (BsonType bsonType : BsonType.values()) {
            if (bsonType.name().equalsIgnoreCase("DECIMAL128")) {
                return true;
            }
        }
        return false;
    }

    public static void recordMongoCollection(SpanEventRecorder recorder, String collectionName, String readPreferenceOrWriteConcern) {
        recorder.recordAttribute(MongoConstants.MONGO_COLLECTION_INFO, collectionName);
        recorder.recordAttribute(MongoConstants.MONGO_COLLECTION_OPTION, readPreferenceOrWriteConcern);
    }

    public static String getWriteConcern0(WriteConcern writeConcern) {

        return mongoWriteConcernMapper.getName(writeConcern);
    }

    public static void recordParsedBson(SpanEventRecorder recorder, NormalizedBson normalizedBson) {
        if (normalizedBson != null) {
            StringStringValue stringStringValue = new StringStringValue(normalizedBson.getNormalizedBson(), normalizedBson.getParameter());
            recorder.recordAttribute(MongoConstants.MONGO_JSON_DATA, stringStringValue);
        }
    }

    public static NormalizedBson parseBson(Object[] args, boolean traceBsonBindValue) {

        if (args == null) {
            return null;
        }

        final List<String> parsedJson = new ArrayList<>(2);
        final List<String> jsonParameter = new ArrayList<>(16);

        for (Object arg : args) {

            WriteContext writeContext = new WriteContext(jsonParameter, decimal128Enabled, traceBsonBindValue);

            String documentString = writeContext.parse(arg);

            if(!documentString.equals(WriteContext.UNTRACED)) {
                parsedJson.add(documentString);
            }
        }

        String parsedJsonString = StringJoinUtils.join(parsedJson, SEPARATOR);
        String jsonParameterString = StringJoinUtils.join(jsonParameter, SEPARATOR);
        return new NormalizedBson(parsedJsonString, jsonParameterString);
    }

    public static List<String> getHostList(MongoClientSettings mongoClientSettings) {
        if (mongoClientSettings.getClusterSettings() == null || mongoClientSettings.getClusterSettings().getHosts() == null) {
            return Collections.emptyList();
        }
        final List<String> hostList = new ArrayList<>();
        for (ServerAddress sa : mongoClientSettings.getClusterSettings().getHosts()) {
            final String hostAddress = HostAndPort.toHostAndPortString(sa.getHost(), sa.getPort());
            hostList.add(hostAddress);
        }

        return hostList;
    }
}

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

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.bson.BsonType;

import com.mongodb.DBObject;
import com.mongodb.WriteConcern;
import com.navercorp.pinpoint.bootstrap.context.SpanEventRecorder;
import com.navercorp.pinpoint.bootstrap.logging.PLogger;
import com.navercorp.pinpoint.bootstrap.logging.PLoggerFactory;
import com.navercorp.pinpoint.common.util.StringStringValue;

/**
 * @author Roy Kim
 */
public final class MongoUtil {

	private static final PLogger LOGGER = PLoggerFactory.getLogger(MongoUtil.class);

    public static final String SEPARATOR = ",";
    private static final MongoWriteConcernMapper mongoWriteConcernMapper = new MongoWriteConcernMapper();

    private static final boolean decimal128Enabled = decimal128Enabled();

    private MongoUtil() {
    }

    //since Mongo Java Driver 3.4
    private static boolean decimal128Enabled() {
        try {
            Class.forName("org.bson.BsonType");
        } catch (ClassNotFoundException e) {
            // MongoDB Driver < 3.X
            return false;
        }

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

        final List<String> parsedJson = new ArrayList<String>(2);
        final List<String> jsonParameter = new ArrayList<String>(16);

        for (Object arg : args) {

            WriteContext writeContext = new WriteContext(jsonParameter, decimal128Enabled, traceBsonBindValue);

            String documentString = writeContext.parse(arg);

            if(!documentString.equals(WriteContext.UNTRACED)) {
                parsedJson.add(documentString);
            }
        }

        String parsedJsonString = StringJoiner.join(parsedJson, SEPARATOR);
        String jsonParameterString = StringJoiner.join(jsonParameter, SEPARATOR);
        return new NormalizedBson(parsedJsonString, jsonParameterString);
    }

    public static NormalizedBson parseJson(Object[] args, boolean traceJsonBindValue) {
        StringBuilder parsedJsonBuilder = new StringBuilder();
        StringBuilder jsonParamBuilder = new StringBuilder();
        parsedJsonBuilder.append("{");
        for (Object arg : args) {
            if (arg instanceof DBObject[]) {
                DBObject[] objects = (DBObject[]) arg;
                for (DBObject object : objects) {
                    extractFromDbObject(parsedJsonBuilder, jsonParamBuilder, object);
                }
            } else if (arg instanceof DBObject) {
                extractFromDbObject(parsedJsonBuilder, jsonParamBuilder, (DBObject) arg);
            } else {
                LOGGER.error("Not a DBObject; it's a(n) " + arg.getClass());
            }
        }

        String parsedJsonString = parsedJsonBuilder.toString();
        String jsonParameterString = jsonParamBuilder.toString();
        LOGGER.info("parsedJsonString - " + parsedJsonString + "; jsonParameterString - " + jsonParameterString);
        return new NormalizedBson(parsedJsonString, jsonParameterString);
    }

    /**
     * @param parsedJsonBuilder
     * @param jsonParamBuilder
     * @param object
     */
    private static void extractFromDbObject(StringBuilder parsedJsonBuilder, StringBuilder jsonParamBuilder,
            DBObject object) {
        Set<String> keySet = object.keySet();
        int size = keySet.size();
        int counter = 0;
        for (String key : keySet) {
            if (key.equals("_id")) {
                continue;
            }
            parsedJsonBuilder.append("\"");
            jsonParamBuilder.append("\"");
            parsedJsonBuilder.append(key);
            parsedJsonBuilder.append("\"");
            parsedJsonBuilder.append(":");
            parsedJsonBuilder.append("\"");
            parsedJsonBuilder.append("?");
            jsonParamBuilder.append(object.get(key));
            parsedJsonBuilder.append("\"");
            jsonParamBuilder.append("\"");

            if (counter != (size - 1)) {
                parsedJsonBuilder.append(",");
                jsonParamBuilder.append(",");
            }
            counter++;
        }
    }
}


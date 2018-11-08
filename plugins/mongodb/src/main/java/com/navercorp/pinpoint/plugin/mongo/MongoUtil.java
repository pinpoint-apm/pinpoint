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

import com.mongodb.BasicDBObject;
import com.mongodb.WriteConcern;
import com.navercorp.pinpoint.bootstrap.context.SpanEventRecorder;
import com.navercorp.pinpoint.common.util.StringStringValue;
import org.bson.BsonDocument;
import org.bson.BsonWriter;
import org.bson.Document;
import org.bson.json.JsonWriter;

import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author Roy Kim
 */
public final class MongoUtil {

    public static final String SEPARATOR = ",";
    private static MongoWriteConcernMapper mongoWriteConcernMapper = new MongoWriteConcernMapper();

    private MongoUtil() {
    }

    public static void recordMongoCollection(SpanEventRecorder recorder, String collectionName, String readPreferenceOrWriteConcern) {
        recorder.recordAttribute(MongoConstants.MONGO_COLLECTION_INFO, collectionName);
        recorder.recordAttribute(MongoConstants.MONGO_COLLECTION_OPTION, readPreferenceOrWriteConcern);
    }

    public static String getWriteConcern0(WriteConcern writeConcern) {

        return mongoWriteConcernMapper.getName(writeConcern);
    }

    public static void recordParsedBson(SpanEventRecorder recorder, StringStringValue stringStringValue) {
        if (stringStringValue != null) {
            recorder.recordAttribute(MongoConstants.MONGO_JSON_DATA, stringStringValue);
        }
    }

    private static Map<String, ?> getBsonKeyValueMap(Object bson) {
        if (bson instanceof BasicDBObject) {
            return (BasicDBObject) bson;
        } else if (bson instanceof BsonDocument) {
            return (BsonDocument) bson;
        } else if (bson instanceof Document) {
            return (Document) bson;
        } else {
            return null;
        }
        //TODO leave comments for further use
//        if(arg instanceof BsonDocumentWrapper) {
//            bson.append(arg.toString());
//        }
//        if(arg instanceof CommandResult) {
//            bson.append(arg.toString());
//        }
//        if(arg instanceof RawBsonDocument) {
//            bson.append(arg.toString());
//        }
    }

    public static StringStringValue parseBson(Object[] args, boolean traceBsonBindValue) {

        if (args == null) {
            return null;
        }

        final List<String> parsedBson = new ArrayList<String>(2);
        final List<String> parameter = new ArrayList<String>(2);

        for (Object arg : args) {

            final Map<String, ?> map = getBsonKeyValueMap(arg);
            if (map == null) {
                continue;
            }

            final Writer writer = new StringWriter();
            final BsonWriter bsonWriter = new JsonWriter(writer);

            bsonWriter.writeStartDocument();
            for (Map.Entry<String, ?> entry : map.entrySet()) {

                bsonWriter.writeName(entry.getKey());
                bsonWriter.writeString("?");

                if (traceBsonBindValue) {
                    final Object value = entry.getValue();
                    String strValue = toStringValue(value);
                    parameter.add(strValue);
                }
            }
            bsonWriter.writeEndDocument();
            bsonWriter.flush();
            String documentString = writer.toString();

            parsedBson.add(documentString);

        }
        String parsedBsonString = StringJoiner.join(parsedBson, SEPARATOR);
        String parameterString = StringJoiner.join(parameter, SEPARATOR);
        return new StringStringValue(parsedBsonString, parameterString);
    }


    private static String toStringValue(Object value) {
        if (value instanceof String) {
            return "\"" + value +"\"";
        } else {
            return String.valueOf(value);
        }
    }
}


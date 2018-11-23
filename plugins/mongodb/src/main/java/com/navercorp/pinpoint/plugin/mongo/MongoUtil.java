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
import com.navercorp.pinpoint.common.util.StringUtils;
import org.bson.BsonDocument;
import org.bson.BsonType;
import org.bson.BsonValue;
import org.bson.BsonWriter;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.bson.json.JsonWriter;

import java.io.StringWriter;
import java.io.Writer;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * @author Roy Kim
 */
public final class MongoUtil {

    public static final String SEPARATOR = ",";
    private static MongoWriteConcernMapper mongoWriteConcernMapper = new MongoWriteConcernMapper();

    private static boolean decimal128Enabled = false;

    private MongoUtil() {
        for (BsonType bsonType : BsonType.values()) {
            if (bsonType.name().equalsIgnoreCase("DECIMAL128")) {
                decimal128Enabled = true;
            }
        }
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

        final List<String> parsedJson = new ArrayList<String>(2);
        final List<String> jsonParameter = new ArrayList<String>(16);

        for (Object arg : args) {

            Writer writer = new StringWriter();
            BsonWriter bsonWriter = new JsonWriter(writer);

            parseBsonObject(jsonParameter, arg, traceBsonBindValue, bsonWriter);

            String documentString = writer.toString();

            parsedJson.add(documentString);
        }

        String parsedJsonString = StringJoiner.join(parsedJson, SEPARATOR);
        String jsonParameterString = StringJoiner.join(jsonParameter, SEPARATOR);
        return new StringStringValue(parsedJsonString, jsonParameterString);
    }

    private static void parseBsonObject(List<String> jsonParameter, Object arg, boolean traceBsonBindValue, BsonWriter bsonWriter) {
        final Map<String, ?> map = getBsonKeyValueMap(arg);
        if (map == null) {
            return;
        }

        bsonWriter.writeStartDocument();
        for (Map.Entry<String, ?> entry : map.entrySet()) {

            bsonWriter.writeName(entry.getKey());

            final Object value = entry.getValue();
            writeValue(jsonParameter, value, traceBsonBindValue, bsonWriter);
        }
        bsonWriter.writeEndDocument();
    }

    private static void parsePrimitiveArrayObject(List<String> jsonParameter, Object arg, boolean traceBsonBindValue, BsonWriter bsonWriter) {
        bsonWriter.writeStartArray();

        int length = Array.getLength(arg);

        for (int i = 0; i < length; i++) {
            writeValue(jsonParameter, Array.get(arg, i), traceBsonBindValue, bsonWriter);
        }
        bsonWriter.writeEndArray();
    }

    private static <T> void parseCollection(List<String> jsonParameter, Collection<T> arg, boolean traceBsonBindValue, BsonWriter bsonWriter) {
        bsonWriter.writeStartArray();

        for (T value : arg) {
            writeValue(jsonParameter, value, traceBsonBindValue, bsonWriter);
        }
        bsonWriter.writeEndArray();
    }

    private static void parseBsonValueObject(List<String> jsonParameter, BsonValue arg, boolean traceBsonBindValue, BsonWriter bsonWriter) {

        BsonType bsonType = arg.getBsonType();

        bsonWriter.writeStartDocument();

        if (bsonType.equals(BsonType.DOUBLE)) {

            bsonWriter.writeName("value");
            writeValue(jsonParameter, arg.asDouble().getValue(), traceBsonBindValue, bsonWriter);

        } else if (bsonType.equals(BsonType.STRING)) {

            bsonWriter.writeName("value");
            writeValue(jsonParameter, arg.asString().getValue(), traceBsonBindValue, bsonWriter);

        } else if (bsonType.equals(BsonType.BINARY)) {

            bsonWriter.writeName("type");
            writeValue(jsonParameter, arg.asBinary().getType(), traceBsonBindValue, bsonWriter);

            bsonWriter.writeName("data");
            writeValue(jsonParameter, arg.asBinary().getData(), traceBsonBindValue, bsonWriter);

        } else if (bsonType.equals(BsonType.OBJECT_ID)) {

            bsonWriter.writeName("value");
            writeValue(jsonParameter, arg.asObjectId().getValue(), traceBsonBindValue, bsonWriter);

        } else if (bsonType.equals(BsonType.BOOLEAN)) {

            bsonWriter.writeName("value");
            writeValue(jsonParameter, arg.asBoolean().getValue(), traceBsonBindValue, bsonWriter);

        } else if (bsonType.equals(BsonType.DATE_TIME)) {

            bsonWriter.writeName("value");
            writeValue(jsonParameter, arg.asDateTime().getValue(), traceBsonBindValue, bsonWriter);

        } else if (bsonType.equals(BsonType.REGULAR_EXPRESSION)) {

            bsonWriter.writeName("pattern");
            writeValue(jsonParameter, arg.asRegularExpression().getPattern(), traceBsonBindValue, bsonWriter);
            bsonWriter.writeName("options");
            writeValue(jsonParameter, arg.asRegularExpression().getOptions(), traceBsonBindValue, bsonWriter);

        } else if (bsonType.equals(BsonType.DB_POINTER)) {

            bsonWriter.writeName("namespace");
            writeValue(jsonParameter, arg.asDBPointer().getNamespace(), traceBsonBindValue, bsonWriter);
            bsonWriter.writeName("id");
            writeValue(jsonParameter, arg.asDBPointer().getId(), traceBsonBindValue, bsonWriter);

        } else if (bsonType.equals(BsonType.JAVASCRIPT)) {

            bsonWriter.writeName("code");
            writeValue(jsonParameter, arg.asJavaScript().getCode(), traceBsonBindValue, bsonWriter);

        } else if (bsonType.equals(BsonType.SYMBOL)) {

            bsonWriter.writeName("symbol");
            writeValue(jsonParameter, arg.asSymbol().getSymbol(), traceBsonBindValue, bsonWriter);

        } else if (bsonType.equals(BsonType.JAVASCRIPT_WITH_SCOPE)) {

            bsonWriter.writeName("code");
            writeValue(jsonParameter, arg.asJavaScriptWithScope().getCode(), traceBsonBindValue, bsonWriter);
            bsonWriter.writeName("scope");
            writeValue(jsonParameter, arg.asJavaScriptWithScope().getScope(), traceBsonBindValue, bsonWriter);

        } else if (bsonType.equals(BsonType.INT32)) {

            bsonWriter.writeName("value");
            writeValue(jsonParameter, arg.asInt32().getValue(), traceBsonBindValue, bsonWriter);

        } else if (bsonType.equals(BsonType.TIMESTAMP)) {

            bsonWriter.writeName("value");
            writeValue(jsonParameter, arg.asTimestamp().getValue(), traceBsonBindValue, bsonWriter);

        } else if (bsonType.equals(BsonType.INT64)) {

            bsonWriter.writeName("value");
            writeValue(jsonParameter, arg.asInt64().getValue(), traceBsonBindValue, bsonWriter);

        } else if (decimal128Enabled && bsonType.equals(BsonType.DECIMAL128)) {

            bsonWriter.writeName("value");
            writeValue(jsonParameter, arg.asDecimal128().getValue(), traceBsonBindValue, bsonWriter);

        }
//        BsonType.DOCUMENT //taken care of in Bson
//        BsonType.ARRAY //taken care of in collection
//        BsonType.END_OF_DOCUMENT //do nothing
//        BsonType.UNDEFINED //do nothing
//        BsonType.NULL //do nothing

        bsonWriter.writeEndDocument();
    }

    private static void writeValue(List<String> jsonParameter, Object arg, boolean traceBsonBindValue, BsonWriter bsonWriter) {
        if (arg instanceof String) {
            bsonWriter.writeString("?");
            if(traceBsonBindValue) {
                jsonParameter.add("\"" + StringUtils.replace((String) arg, "\"", "\"\"") + "\"");
            }
        } else if (arg.getClass().isArray()) {
            parsePrimitiveArrayObject(jsonParameter, arg, traceBsonBindValue, bsonWriter);
        } else if (arg instanceof Collection) {
            parseCollection(jsonParameter, (Collection) arg, traceBsonBindValue, bsonWriter);
        } else if (arg instanceof Bson) {
            parseBsonObject(jsonParameter, arg, traceBsonBindValue, bsonWriter);
        } else if (arg instanceof BsonValue) {
            parseBsonValueObject(jsonParameter, (BsonValue) arg, traceBsonBindValue, bsonWriter);
        } else {
            bsonWriter.writeString("?");
            if(traceBsonBindValue) {
                jsonParameter.add(String.valueOf(arg));
            }
        }
    }
}


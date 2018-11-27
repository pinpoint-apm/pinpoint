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
import com.navercorp.pinpoint.common.util.Assert;
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
    private static final MongoWriteConcernMapper mongoWriteConcernMapper = new MongoWriteConcernMapper();

    private static final boolean decimal128Enabled = decimal128Enabled();

    private MongoUtil() {
    }

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

            WriteContext writeContext = new WriteContext(jsonParameter, decimal128Enabled, traceBsonBindValue);
            String documentString = writeContext.parse(arg);

            parsedJson.add(documentString);
        }

        String parsedJsonString = StringJoiner.join(parsedJson, SEPARATOR);
        String jsonParameterString = StringJoiner.join(jsonParameter, SEPARATOR);
        return new StringStringValue(parsedJsonString, jsonParameterString);
    }

    private static class WriteContext {

        private final Writer writer = new StringWriter();
        private final BsonWriter bsonWriter = new JsonWriter(writer);

        private final List<String> jsonParameter;

        private final boolean traceBsonBindValue;
        private final boolean decimal128Enabled;

        public WriteContext(List<String> jsonParameterAppender, boolean decimal128Enabled, boolean traceBsonBindValue) {
            this.jsonParameter = Assert.requireNonNull(jsonParameterAppender, "jsonParameterAppender must not be null");
            this.decimal128Enabled = decimal128Enabled;
            this.traceBsonBindValue = traceBsonBindValue;
        }

        public String parse(Object arg) {
            parseBsonObject(arg);
            return writer.toString();
        }

        private void parseBsonObject(Object arg) {
            final Map<String, ?> map = getBsonKeyValueMap(arg);
            if (map == null) {
                return;
            }

            bsonWriter.writeStartDocument();
            for (Map.Entry<String, ?> entry : map.entrySet()) {

                bsonWriter.writeName(entry.getKey());

                final Object value = entry.getValue();
                writeValue(value);
            }
            bsonWriter.writeEndDocument();
        }

        private void parsePrimitiveArrayObject(Object arg) {
            bsonWriter.writeStartArray();

            int length = Array.getLength(arg);

            for (int i = 0; i < length; i++) {
                writeValue(Array.get(arg, i));
            }
            bsonWriter.writeEndArray();
        }

        private <T> void parseCollection(Collection<T> arg) {
            bsonWriter.writeStartArray();

            for (T value : arg) {
                writeValue(value);
            }
            bsonWriter.writeEndArray();
        }

        private void parseBsonValueObject(BsonValue arg) {

            BsonType bsonType = arg.getBsonType();

            bsonWriter.writeStartDocument();

            if (bsonType.equals(BsonType.DOUBLE)) {

                bsonWriter.writeName("value");
                writeValue(arg.asDouble().getValue());

            } else if (bsonType.equals(BsonType.STRING)) {

                bsonWriter.writeName("value");
                writeValue(arg.asString().getValue());

            } else if (bsonType.equals(BsonType.BINARY)) {

                bsonWriter.writeName("type");
                writeValue(arg.asBinary().getType());

                bsonWriter.writeName("data");
                writeValue(arg.asBinary().getData());

            } else if (bsonType.equals(BsonType.OBJECT_ID)) {

                bsonWriter.writeName("value");
                writeValue(arg.asObjectId().getValue());

            } else if (bsonType.equals(BsonType.BOOLEAN)) {

                bsonWriter.writeName("value");
                writeValue(arg.asBoolean().getValue());

            } else if (bsonType.equals(BsonType.DATE_TIME)) {

                bsonWriter.writeName("value");
                writeValue(arg.asDateTime().getValue());

            } else if (bsonType.equals(BsonType.REGULAR_EXPRESSION)) {

                bsonWriter.writeName("pattern");
                writeValue(arg.asRegularExpression().getPattern());
                bsonWriter.writeName("options");
                writeValue(arg.asRegularExpression().getOptions());

            } else if (bsonType.equals(BsonType.DB_POINTER)) {

                bsonWriter.writeName("namespace");
                writeValue(arg.asDBPointer().getNamespace());
                bsonWriter.writeName("id");
                writeValue(arg.asDBPointer().getId());

            } else if (bsonType.equals(BsonType.JAVASCRIPT)) {

                bsonWriter.writeName("code");
                writeValue(arg.asJavaScript().getCode());

            } else if (bsonType.equals(BsonType.SYMBOL)) {

                bsonWriter.writeName("symbol");
                writeValue(arg.asSymbol().getSymbol());

            } else if (bsonType.equals(BsonType.JAVASCRIPT_WITH_SCOPE)) {

                bsonWriter.writeName("code");
                writeValue(arg.asJavaScriptWithScope().getCode());
                bsonWriter.writeName("scope");
                writeValue(arg.asJavaScriptWithScope().getScope());

            } else if (bsonType.equals(BsonType.INT32)) {

                bsonWriter.writeName("value");
                writeValue(arg.asInt32().getValue());

            } else if (bsonType.equals(BsonType.TIMESTAMP)) {

                bsonWriter.writeName("value");
                writeValue(arg.asTimestamp().getValue());

            } else if (bsonType.equals(BsonType.INT64)) {

                bsonWriter.writeName("value");
                writeValue(arg.asInt64().getValue());

            } else if (decimal128Enabled && bsonType.equals(BsonType.DECIMAL128)) {

                bsonWriter.writeName("value");
                writeValue(arg.asDecimal128().getValue());

            }
//        BsonType.DOCUMENT //taken care of in Bson
//        BsonType.ARRAY //taken care of in collection
//        BsonType.END_OF_DOCUMENT //do nothing
//        BsonType.UNDEFINED //do nothing
//        BsonType.NULL //do nothing

            bsonWriter.writeEndDocument();
        }

        private void writeValue(Object arg) {
            if (arg instanceof String) {
                bsonWriter.writeString("?");
                if (traceBsonBindValue) {
                    jsonParameter.add("\"" + StringUtils.replace((String) arg, "\"", "\"\"") + "\"");
                }
            } else if (arg.getClass().isArray()) {
                parsePrimitiveArrayObject(arg);
            } else if (arg instanceof Collection) {
                parseCollection((Collection) arg);
            } else if (arg instanceof Bson) {
                parseBsonObject(arg);
            } else if (arg instanceof BsonValue) {
                parseBsonValueObject((BsonValue) arg);
            } else {
                bsonWriter.writeString("?");
                if (traceBsonBindValue) {
                    jsonParameter.add(String.valueOf(arg));
                }
            }
        }
    }

}
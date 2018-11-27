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
import org.bson.internal.Base64;
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
    private static final boolean base64ClassEnabled = base64ClassEnabled();

    private static final int DEFAULT_ABBREVIATE_MAX_WIDTH = 16;

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

    //since Mongo Java Driver 3.5
    private static boolean base64ClassEnabled() {
        try {
            Class.forName("org.bson.internal.Base64");
            return true;
        } catch (ClassNotFoundException e) {
            return false;
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

                String key = entry.getKey();
                if (key.equals("_id")) {
                    continue;
                }
                bsonWriter.writeName(key);

                writeValue(entry.getValue());
            }
            bsonWriter.writeEndDocument();
        }

        private void parsePrimitiveArrayObject(Object arg) {
            bsonWriter.writeStartArray();

            arrayAbbreviationForMongo(arg);

            bsonWriter.writeEndArray();
        }

        private <T> void parseCollection(Collection<T> arg) {
            bsonWriter.writeStartArray();

            collectionAbbreviationForMongo(arg);

            bsonWriter.writeEndArray();
        }

        private void parseBsonValueObject(BsonValue arg) {

            BsonType bsonType = arg.getBsonType();

            //write with same format of JsonWriter(JsonMode.STRICT)
            if (bsonType.equals(BsonType.DOUBLE)) {

                writeValue(arg.asDouble().getValue());

            } else if (bsonType.equals(BsonType.STRING)) {

                writeValue(arg.asString().getValue());

            } else if (bsonType.equals(BsonType.BINARY)) {

                String abbreviatedBinary = bynaryAbbreviationForMongo(arg);
                bsonWriter.writeStartDocument();
                bsonWriter.writeName("$binary");
                writeValue(abbreviatedBinary);

                bsonWriter.writeName("$type");
                writeValue(String.valueOf(String.format("%02X", arg.asBinary().getType())));
                bsonWriter.writeEndDocument();

            } else if (bsonType.equals(BsonType.OBJECT_ID)) {

                bsonWriter.writeStartDocument();
                bsonWriter.writeName("$oid");
                writeValue(String.valueOf(arg.asObjectId().getValue()));
                bsonWriter.writeEndDocument();

            } else if (bsonType.equals(BsonType.BOOLEAN)) {

                writeValue(arg.asBoolean().getValue());

            } else if (bsonType.equals(BsonType.DATE_TIME)) {

                bsonWriter.writeStartDocument();
                bsonWriter.writeName("$date");
                writeValue(arg.asDateTime().getValue());
                bsonWriter.writeEndDocument();

            } else if (bsonType.equals(BsonType.REGULAR_EXPRESSION)) {

                bsonWriter.writeStartDocument();
                bsonWriter.writeName("$regex");
                writeValue(arg.asRegularExpression().getPattern());
                bsonWriter.writeName("$options");
                writeValue(arg.asRegularExpression().getOptions());
                bsonWriter.writeEndDocument();

            } else if (bsonType.equals(BsonType.DB_POINTER)) {

                bsonWriter.writeStartDocument();
                bsonWriter.writeName("$ref");
                writeValue(arg.asDBPointer().getNamespace());
                bsonWriter.writeName("$id");

                bsonWriter.writeStartDocument();
                bsonWriter.writeName("$oid");
                writeValue(String.valueOf(arg.asDBPointer().getId()));
                bsonWriter.writeEndDocument();

                bsonWriter.writeEndDocument();

            } else if (bsonType.equals(BsonType.JAVASCRIPT)) {

                bsonWriter.writeStartDocument();
                bsonWriter.writeName("$code");
                writeValue(arg.asJavaScript().getCode());
                bsonWriter.writeEndDocument();

            } else if (bsonType.equals(BsonType.SYMBOL)) {

                bsonWriter.writeStartDocument();
                bsonWriter.writeName("$symbol");
                writeValue(arg.asSymbol().getSymbol());
                bsonWriter.writeEndDocument();

            } else if (bsonType.equals(BsonType.JAVASCRIPT_WITH_SCOPE)) {

                bsonWriter.writeStartDocument();
                bsonWriter.writeName("$code");
                writeValue(arg.asJavaScriptWithScope().getCode());
                bsonWriter.writeName("$scope");
                writeValue(arg.asJavaScriptWithScope().getScope());
                bsonWriter.writeEndDocument();

            } else if (bsonType.equals(BsonType.INT32)) {

                writeValue(arg.asInt32().getValue());

            } else if (bsonType.equals(BsonType.TIMESTAMP)) {

                bsonWriter.writeStartDocument();
                bsonWriter.writeName("$timestamp");

                bsonWriter.writeStartDocument();
                bsonWriter.writeName("t");
                writeValue(arg.asTimestamp().getTime());
                bsonWriter.writeName("i");
                writeValue(arg.asTimestamp().getInc());
                bsonWriter.writeEndDocument();

                bsonWriter.writeEndDocument();

            } else if (bsonType.equals(BsonType.INT64)) {

                bsonWriter.writeStartDocument();
                bsonWriter.writeName("$numberLong");
                writeValue(String.valueOf(arg.asInt64().getValue()));
                bsonWriter.writeEndDocument();

            } else if (bsonType.equals(BsonType.UNDEFINED)) {

                bsonWriter.writeStartDocument();
                bsonWriter.writeName("$undefined");
                writeValue(true);
                bsonWriter.writeEndDocument();

            } else if (bsonType.equals(BsonType.NULL)) {

                writeValue(null);

            } else if (decimal128Enabled && bsonType.equals(BsonType.DECIMAL128)) {

                //since Mongo Java Driver 3.4
                bsonWriter.writeStartDocument();
                bsonWriter.writeName("$numberDecimal");
                writeValue(String.valueOf(arg.asDecimal128().getValue()));
                bsonWriter.writeEndDocument();
            }
//        BsonType.DOCUMENT //taken care of in Bson
//        BsonType.ARRAY //taken care of in collection
//        BsonType.END_OF_DOCUMENT //do nothing

        }

        private String bynaryAbbreviationForMongo(BsonValue arg) {

            byte[] binary = arg.asBinary().getData();
            int binaryLength = binary.length;

            if (binaryLength > DEFAULT_ABBREVIATE_MAX_WIDTH) {
                byte[] smallerData = new byte[DEFAULT_ABBREVIATE_MAX_WIDTH];
                System.arraycopy(binary, 0, smallerData, 0, DEFAULT_ABBREVIATE_MAX_WIDTH);

                if (base64ClassEnabled) {
                    //since Mongo Java Driver 3.5
                    return Base64.encode(smallerData) + "...(" + binaryLength + ")";
                } else {
                    //since Mongo Driver Up to 3.0
                    return _printBase64Binary(smallerData) + "...(" + binaryLength + ")";
                }
            } else {
                if (base64ClassEnabled) {
                    //since Mongo Java Driver 3.5
                    return Base64.encode(binary);
                } else {
                    //since Mongo Driver Up to 3.0
                    return _printBase64Binary(binary);
                }
            }
        }

        private void arrayAbbreviationForMongo(Object arg) {
            int length = Array.getLength(arg);
            for (int i = 0; i < length && i < DEFAULT_ABBREVIATE_MAX_WIDTH - 1; i++) {
                writeValue(Array.get(arg, i));
            }
            if (length > DEFAULT_ABBREVIATE_MAX_WIDTH - 2) {
                bsonWriter.writeString("?");
                if (traceBsonBindValue) {
                    jsonParameter.add("...(" + length + ")");
                }
            }
        }

        private <T> void collectionAbbreviationForMongo(Collection<T> arg) {
            int length = arg.size();
            int i = 0;
            for (T value : arg) {
                writeValue(value);
                i++;
                if (i > DEFAULT_ABBREVIATE_MAX_WIDTH - 2) {
                    break;
                }
            }
            if (length > DEFAULT_ABBREVIATE_MAX_WIDTH - 2) {
                bsonWriter.writeString("?");
                if (traceBsonBindValue) {
                    jsonParameter.add("...(" + length + ")");
                }
            }
        }

        private void writeValue(Object arg) {
            if (arg == null) {
                bsonWriter.writeString("?");
                if (traceBsonBindValue) {
                    jsonParameter.add(String.valueOf(arg));
                }
            } else if (arg instanceof String) {
                bsonWriter.writeString("?");
                if (traceBsonBindValue) {
                    jsonParameter.add("\"" + StringUtils.abbreviate(StringUtils.replace((String) arg, "\"", "\"\"")) + "\"");
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
                    jsonParameter.add(StringUtils.abbreviate(String.valueOf(arg)));
                }
            }
        }

        /**
         * from javax.xml.bind jdk8
         * checked compatibility with jdk6
         **/
        private static final char[] encodeMap = initEncodeMap();

        private static char[] initEncodeMap() {
            char[] map = new char[64];
            int i;
            for (i = 0; i < 26; i++) {
                map[i] = (char) ('A' + i);
            }
            for (i = 26; i < 52; i++) {
                map[i] = (char) ('a' + (i - 26));
            }
            for (i = 52; i < 62; i++) {
                map[i] = (char) ('0' + (i - 52));
            }
            map[62] = '+';
            map[63] = '/';

            return map;
        }

        public static String _printBase64Binary(byte[] input) {
            return _printBase64Binary(input, 0, input.length);
        }

        public static String _printBase64Binary(byte[] input, int offset, int len) {
            char[] buf = new char[((len + 2) / 3) * 4];
            int ptr = _printBase64Binary(input, offset, len, buf, 0);
            assert ptr == buf.length;
            return new String(buf);
        }

        /**
         * Encodes a byte array into a char array by doing base64 encoding.
         * <p>
         * The caller must supply a big enough buffer.
         *
         * @return the value of {@code ptr+((len+2)/3)*4}, which is the new offset
         * in the output buffer where the further bytes should be placed.
         */
        public static int _printBase64Binary(byte[] input, int offset, int len, char[] buf, int ptr) {
            // encode elements until only 1 or 2 elements are left to encode
            int remaining = len;
            int i;
            for (i = offset; remaining >= 3; remaining -= 3, i += 3) {
                buf[ptr++] = encode(input[i] >> 2);
                buf[ptr++] = encode(
                        ((input[i] & 0x3) << 4)
                                | ((input[i + 1] >> 4) & 0xF));
                buf[ptr++] = encode(
                        ((input[i + 1] & 0xF) << 2)
                                | ((input[i + 2] >> 6) & 0x3));
                buf[ptr++] = encode(input[i + 2] & 0x3F);
            }
            // encode when exactly 1 element (left) to encode
            if (remaining == 1) {
                buf[ptr++] = encode(input[i] >> 2);
                buf[ptr++] = encode(((input[i]) & 0x3) << 4);
                buf[ptr++] = '=';
                buf[ptr++] = '=';
            }
            // encode when exactly 2 elements (left) to encode
            if (remaining == 2) {
                buf[ptr++] = encode(input[i] >> 2);
                buf[ptr++] = encode(((input[i] & 0x3) << 4)
                        | ((input[i + 1] >> 4) & 0xF));
                buf[ptr++] = encode((input[i + 1] & 0xF) << 2);
                buf[ptr++] = '=';
            }
            return ptr;
        }

        public static char encode(int i) {
            return encodeMap[i & 0x3F];
        }
    }
}


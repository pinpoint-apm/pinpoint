package com.navercorp.pinpoint.plugin.mongo;
/*
 * Copyright 2019 NAVER Corp.
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

import com.mongodb.BasicDBObject;
import com.mongodb.client.model.PushOptions;
import com.mongodb.client.model.TextSearchOptions;
import com.navercorp.pinpoint.bootstrap.logging.PLogger;
import com.navercorp.pinpoint.bootstrap.logging.PLoggerFactory;
import com.navercorp.pinpoint.common.util.StringUtils;
import com.navercorp.pinpoint.plugin.mongo.field.getter.ExtendedBsonListGetter;
import com.navercorp.pinpoint.plugin.mongo.field.getter.FieldNameGetter;
import com.navercorp.pinpoint.plugin.mongo.field.getter.OperatorGetter;
import com.navercorp.pinpoint.plugin.mongo.field.getter.ValueGetter;
import com.navercorp.pinpoint.plugin.mongo.field.getter.filters.BsonValueGetter;
import com.navercorp.pinpoint.plugin.mongo.field.getter.filters.FilterGetter;
import com.navercorp.pinpoint.plugin.mongo.field.getter.filters.FiltersGetter;
import com.navercorp.pinpoint.plugin.mongo.field.getter.filters.GeometryGetter;
import com.navercorp.pinpoint.plugin.mongo.field.getter.filters.InternalOperatorNameAccessor;
import com.navercorp.pinpoint.plugin.mongo.field.getter.filters.IterableValuesGetter;
import com.navercorp.pinpoint.plugin.mongo.field.getter.filters.MaxDistanceGetter;
import com.navercorp.pinpoint.plugin.mongo.field.getter.filters.MinDistanceGetter;
import com.navercorp.pinpoint.plugin.mongo.field.getter.filters.OperatorNameGetter;
import com.navercorp.pinpoint.plugin.mongo.field.getter.filters.SearchGetter;
import com.navercorp.pinpoint.plugin.mongo.field.getter.filters.TextSearchOptionsGetter;
import com.navercorp.pinpoint.plugin.mongo.field.getter.updates.ListValuesGetter;
import com.navercorp.pinpoint.plugin.mongo.field.getter.updates.PushOptionsGetter;
import org.bson.BsonBinary;
import org.bson.BsonBoolean;
import org.bson.BsonDateTime;
import org.bson.BsonDbPointer;
import org.bson.BsonDocument;
import org.bson.BsonDouble;
import org.bson.BsonInt32;
import org.bson.BsonInt64;
import org.bson.BsonJavaScript;
import org.bson.BsonJavaScriptWithScope;
import org.bson.BsonRegularExpression;
import org.bson.BsonString;
import org.bson.BsonSymbol;
import org.bson.BsonTimestamp;
import org.bson.BsonType;
import org.bson.BsonValue;
import org.bson.BsonWriter;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.bson.json.JsonWriter;

import java.io.StringWriter;
import java.io.Writer;
import java.lang.reflect.Array;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Base64;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * @author Roy Kim
 */
class WriteContext {
    private final PLogger logger = PLoggerFactory.getLogger(this.getClass());
    private final Writer writer = new StringWriter();

    private final BsonWriter bsonWriter = new JsonWriter(writer);

    private final List<String> jsonParameter;

    private final boolean traceBsonBindValue;
    private final boolean decimal128Enabled;

    static final int DEFAULT_ABBREVIATE_MAX_WIDTH = 8;
    static final String UNTRACED = "Unsupported-trace";

    public WriteContext(List<String> jsonParameterAppender, boolean decimal128Enabled, boolean traceBsonBindValue) {
        this.jsonParameter = Objects.requireNonNull(jsonParameterAppender, "jsonParameterAppender");
        this.decimal128Enabled = decimal128Enabled;
        this.traceBsonBindValue = traceBsonBindValue;
    }

    public String parse(Object arg) {
        if (arg instanceof Bson) {
            Bson bson = (Bson) arg;
            writeBsonObject(bson);
        } else if (arg instanceof List) {
            final List<?> list = (List<?>) arg;
            if (list.get(0) instanceof Bson) {
                bsonWriter.writeStartDocument();
                bsonWriter.writeName("bsons");
                writeValue(arg);
                bsonWriter.writeEndDocument();
            } else {
                logger.debug(UNTRACED);
                return UNTRACED;
            }
        } else {
            logger.debug(UNTRACED);
            return UNTRACED;
        }

        return writer.toString();
    }

    private void parseUpdatesObject(Object arg) {
        String argName = arg.getClass().getName();

        //SimpleUpdate
        switch (argName) {
            case MongoConstants.MONGO_UPDATES_SIMPLE:
                logger.debug("writing SimpleUpdate");

                bsonWriter.writeStartDocument();
                bsonWriter.writeName(((OperatorGetter) arg)._$PINPOINT$_getOperator());

                bsonWriter.writeStartDocument();
                bsonWriter.writeName(((FieldNameGetter) arg)._$PINPOINT$_getFieldName());
                writeValue(((ValueGetter) arg)._$PINPOINT$_getValue());
                bsonWriter.writeEndDocument();

                bsonWriter.writeEndDocument();
                break;
            //WithEachUpdate
            case MongoConstants.MONGO_UPDATES_WITHEACH:
                logger.debug("writing WithEachUpdate");

                bsonWriter.writeStartDocument();
                bsonWriter.writeName(((OperatorGetter) arg)._$PINPOINT$_getOperator());

                bsonWriter.writeStartDocument();
                bsonWriter.writeName(((FieldNameGetter) arg)._$PINPOINT$_getFieldName());
                bsonWriter.writeStartDocument();

                bsonWriter.writeStartArray("$each");
                for (Object value : ((ListValuesGetter) arg)._$PINPOINT$_getValues()) {
                    writeValue(value);
                }
                bsonWriter.writeEndArray();
                bsonWriter.writeEndDocument();
                bsonWriter.writeEndDocument();
                bsonWriter.writeEndDocument();
                break;
            //PushUpdate
            case MongoConstants.MONGO_UPDATES_PUSH:
                logger.debug("writing PushUpdate");

                bsonWriter.writeStartDocument();
                bsonWriter.writeName(((OperatorGetter) arg)._$PINPOINT$_getOperator());

                bsonWriter.writeStartDocument();
                bsonWriter.writeName(((FieldNameGetter) arg)._$PINPOINT$_getFieldName());
                bsonWriter.writeStartDocument();

                bsonWriter.writeStartArray("$each");
                for (Object value : ((ListValuesGetter) arg)._$PINPOINT$_getValues()) {
                    writeValue(value);
                }
                bsonWriter.writeEndArray();

                PushOptions options = ((PushOptionsGetter) arg)._$PINPOINT$_getPushOptions();
                if (options != null) {
                    if (options.getPosition() != null) {
                        bsonWriter.writeInt32("$position", options.getPosition());
                    }
                    if (options.getSlice() != null) {
                        bsonWriter.writeInt32("$slice", options.getSlice());
                    }
                    if (options.getSort() != null) {
                        bsonWriter.writeInt32("$sort", options.getSort());
                    } else if (options.getSortDocument() != null) {
                        bsonWriter.writeName("$sort");
                        writeBsonObject(options.getSortDocument());
                    }
                }
                bsonWriter.writeEndDocument();
                bsonWriter.writeEndDocument();
                bsonWriter.writeEndDocument();
                break;
            //PullAllUpdate
            case MongoConstants.MONGO_UPDATES_PULLALL:
                logger.debug("writing PullAllUpdate");

                bsonWriter.writeStartDocument();
                bsonWriter.writeName("$pullAll");

                bsonWriter.writeStartDocument();
                bsonWriter.writeName(((FieldNameGetter) arg)._$PINPOINT$_getFieldName());

                bsonWriter.writeStartArray();
                for (Object value : ((ListValuesGetter) arg)._$PINPOINT$_getValues()) {
                    writeValue(value);
                }
                bsonWriter.writeEndArray();

                bsonWriter.writeEndDocument();

                bsonWriter.writeEndDocument();
                break;
            //CompositeUpdate
            case MongoConstants.MONGO_UPDATES_COMPOSITE:
                logger.debug("writing CompositeUpdate");

                bsonWriter.writeStartDocument();

                bsonWriter.writeStartArray("$updates");
                for (Bson value : ((ExtendedBsonListGetter) arg)._$PINPOINT$_getExtendedBsonList()) {
                    writeBsonObject(value);
                }
                bsonWriter.writeEndArray();
                bsonWriter.writeEndDocument();
                break;
        }

    }

    private void parseSortObject(Object arg) {
        String argName = arg.getClass().getName();

        //CompoundSort
        if (argName.equals(MongoConstants.MONGO_SORT_COMPOSITE)) {
            logger.debug("writing CompoundSort");

            bsonWriter.writeStartDocument();

            bsonWriter.writeStartArray("$sorts");
            for (Bson value : ((ExtendedBsonListGetter) arg)._$PINPOINT$_getExtendedBsonList()) {
                writeBsonObject(value);
            }
            bsonWriter.writeEndArray();
            bsonWriter.writeEndDocument();
        }
    }

    private void parseFilterObject(Object arg) {

        String argName = arg.getClass().getName();
        logger.debug("filter arg : {}", argName);

        //OperatorFilter
        switch (argName) {
            case MongoConstants.MONGO_FILTER_GEOMETRY_OPERATOR:
                logger.debug("writing GeometryOperatorFilter");

                bsonWriter.writeStartDocument();
                bsonWriter.writeName(((FieldNameGetter) arg)._$PINPOINT$_getFieldName());
                bsonWriter.writeStartDocument();
                bsonWriter.writeName(((OperatorNameGetter) arg)._$PINPOINT$_getOperatorName());
                bsonWriter.writeStartDocument();
                bsonWriter.writeName("$geometry");

                writeValue(((GeometryGetter) arg)._$PINPOINT$_getGeometry());

                if (((MaxDistanceGetter) arg)._$PINPOINT$_getMaxDistance() != null) {
                    bsonWriter.writeDouble("$maxDistance", ((MaxDistanceGetter) arg)._$PINPOINT$_getMaxDistance());
                }

                if (((MinDistanceGetter) arg)._$PINPOINT$_getMinDistance() != null) {
                    bsonWriter.writeDouble("$minDistance", ((MinDistanceGetter) arg)._$PINPOINT$_getMinDistance());
                }

                bsonWriter.writeEndDocument();
                bsonWriter.writeEndDocument();
                bsonWriter.writeEndDocument();
                break;
            //OperatorFilter
            case MongoConstants.MONGO_FILTER_OPERATOR:
                logger.debug("writing OperatorFilter");

                bsonWriter.writeStartDocument();
                bsonWriter.writeName(((FieldNameGetter) arg)._$PINPOINT$_getFieldName());
                bsonWriter.writeStartDocument();
                bsonWriter.writeName(((OperatorNameGetter) arg)._$PINPOINT$_getOperatorName());
                writeValue(((ValueGetter) arg)._$PINPOINT$_getValue());
                bsonWriter.writeEndDocument();
                bsonWriter.writeEndDocument();
                break;
            //IterableOperatorFilter
            case MongoConstants.MONGO_FILTER_ITERABLE_OPERATOR:
                logger.debug("writing IterableOperatorFilter");

                if (arg instanceof FieldNameGetter) {
                    bsonWriter.writeStartDocument();
                    bsonWriter.writeName(((FieldNameGetter) arg)._$PINPOINT$_getFieldName());
                }

                bsonWriter.writeStartDocument();
                bsonWriter.writeName(((OperatorNameGetter) arg)._$PINPOINT$_getOperatorName());

                writeValue(((IterableValuesGetter) arg)._$PINPOINT$_getValues());

                bsonWriter.writeEndDocument();

                if (arg instanceof FieldNameGetter) {
                    bsonWriter.writeEndDocument();
                }
                break;
            //SimpleEncodingFilter
            case MongoConstants.MONGO_FILTER_SIMPLE_ENCODING:
                //else if (arg instanceof FieldNameGetter && arg instanceof ValueGetter) {

                logger.debug("writing SimpleEncodingFilter");

                bsonWriter.writeStartDocument();
                bsonWriter.writeName(((FieldNameGetter) arg)._$PINPOINT$_getFieldName());
                writeValue(((ValueGetter) arg)._$PINPOINT$_getValue());
                bsonWriter.writeEndDocument();
                break;

            //SimpleFilter
            case MongoConstants.MONGO_FILTER_SIMPLE:

                logger.debug("writing SimpleFilter");

                bsonWriter.writeStartDocument();
                bsonWriter.writeName(((FieldNameGetter) arg)._$PINPOINT$_getFieldName());
                BsonValue bsonValue = ((BsonValueGetter) arg)._$PINPOINT$_getValue();
                writeBsonValueObject(bsonValue);
                bsonWriter.writeEndDocument();
                break;

            //AndFilter
            case MongoConstants.MONGO_FILTER_AND:
                logger.debug("writing AndFilter");

                bsonWriter.writeStartDocument();

                bsonWriter.writeName("$and");
                bsonWriter.writeStartArray();
                for (Bson bsonFilter : ((FiltersGetter) arg)._$PINPOINT$_getFilters()) {
                    logger.debug("writing filters");
                    writeBsonObject(bsonFilter);
                }
                bsonWriter.writeEndArray();
                bsonWriter.writeEndDocument();
                break;

            //NotFilter
            case MongoConstants.MONGO_FILTER_NOT:
                logger.debug("writing NotFilter");

                bsonWriter.writeStartDocument();

                bsonWriter.writeName("$not");
                writeBsonObject(((FilterGetter) arg)._$PINPOINT$_getFilter());
                bsonWriter.writeEndDocument();
                break;

            //TextFilter
            case MongoConstants.MONGO_FILTER_TEXT:
                logger.debug("writing TextFilter");

                bsonWriter.writeStartDocument();
                bsonWriter.writeName("$text");
                bsonWriter.writeStartDocument();
                bsonWriter.writeName("$search");
                writeString(((SearchGetter) arg)._$PINPOINT$_getSearch());

                TextSearchOptions textSearchOptions = ((TextSearchOptionsGetter) arg)._$PINPOINT$_getTextSearchOptions();
                if (textSearchOptions.getLanguage() != null) {
                    bsonWriter.writeName("$language");
                    writeString(textSearchOptions.getLanguage());
                }
                if (textSearchOptions.getCaseSensitive() != null) {
                    bsonWriter.writeName("$caseSensitive");
                    writeBoolean(textSearchOptions.getCaseSensitive());
                }
                if (textSearchOptions.getDiacriticSensitive() != null) {
                    bsonWriter.writeName("$diacriticSensitive");
                    writeBoolean(textSearchOptions.getDiacriticSensitive());
                }

                bsonWriter.writeEndDocument();
                bsonWriter.writeEndDocument();
                break;

            //OrNorFilter
            case MongoConstants.MONGO_FILTER_ORNOR:
                logger.debug("writing OrNorFilter");
                String input = setInput(arg);

                bsonWriter.writeStartDocument();
                bsonWriter.writeName(input);
                bsonWriter.writeStartArray();
                for (Bson bsonFilter : ((FiltersGetter) arg)._$PINPOINT$_getFilters()) {
                    writeBsonObject(bsonFilter);
                }
                bsonWriter.writeEndArray();
                bsonWriter.writeEndDocument();
                break;
        }
    }

    private String setInput(Object arg) {
        String input = "$or/nor";
        if (arg instanceof InternalOperatorNameAccessor) {
            String op = ((InternalOperatorNameAccessor) arg)._$PINPOINT$_getInternalOperatorName();
            if (op.equals("OR")) {
                input = "$or";
            } else if (op.equals("NOR")) {
                input = "$nor";
            }
        }
        return input;
    }

    private void writeBsonObject(Bson bson) {
        final Map<String, ?> map = getBsonKeyValueMap(bson);
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

    private void writePrimitiveArrayObject(Object arrayObject) {
        bsonWriter.writeStartArray();

        arrayAbbreviationForMongo(arrayObject);

        bsonWriter.writeEndArray();
    }

    private <T> void writeCollection(Collection<T> arg) {
        bsonWriter.writeStartArray();

        collectionAbbreviationForMongo(arg);

        bsonWriter.writeEndArray();
    }

    private void writeBsonValueObject(BsonValue arg) {

        BsonType bsonType = arg.getBsonType();

        //write with same format of JsonWriter(JsonMode.STRICT)
        if (bsonType.equals(BsonType.DOUBLE)) {

            BsonDouble bsonDouble = arg.asDouble();
            writeDouble(bsonDouble.getValue());

        } else if (bsonType.equals(BsonType.STRING)) {

            BsonString bsonString = arg.asString();
            writeString(bsonString.getValue());

        } else if (bsonType.equals(BsonType.BINARY)) {
            BsonBinary bsonBinary = (BsonBinary) arg;
            String abbreviatedBinary = binaryAbbreviationForMongo(bsonBinary);
            bsonWriter.writeStartDocument();
            bsonWriter.writeName("$binary");
            writeString(abbreviatedBinary);

            bsonWriter.writeName("$type");
            writeString(String.valueOf(String.format("%02X", bsonBinary.getType())));
            bsonWriter.writeEndDocument();

        } else if (bsonType.equals(BsonType.OBJECT_ID)) {

            bsonWriter.writeStartDocument();
            bsonWriter.writeName("$oid");
            writeString(String.valueOf(arg.asObjectId().getValue()));
            bsonWriter.writeEndDocument();

        } else if (bsonType.equals(BsonType.BOOLEAN)) {

            BsonBoolean bsonBoolean = arg.asBoolean();
            writeBoolean(bsonBoolean.getValue());

        } else if (bsonType.equals(BsonType.DATE_TIME)) {

            BsonDateTime bsonDateTime = arg.asDateTime();
            bsonWriter.writeStartDocument();
            bsonWriter.writeName("$date");
            writeInt64(bsonDateTime.getValue());
            bsonWriter.writeEndDocument();

        } else if (bsonType.equals(BsonType.REGULAR_EXPRESSION)) {

            BsonRegularExpression bsonRegularExpression = arg.asRegularExpression();

            bsonWriter.writeStartDocument();
            bsonWriter.writeName("$regex");
            writeString(bsonRegularExpression.getPattern());
            bsonWriter.writeName("$options");
            writeString(bsonRegularExpression.getOptions());
            bsonWriter.writeEndDocument();

        } else if (bsonType.equals(BsonType.DB_POINTER)) {

            BsonDbPointer bsonDbPointer = arg.asDBPointer();

            bsonWriter.writeStartDocument();
            bsonWriter.writeName("$ref");
            writeString(bsonDbPointer.getNamespace());
            bsonWriter.writeName("$id");

            bsonWriter.writeStartDocument();
            bsonWriter.writeName("$oid");
            writeString(String.valueOf(bsonDbPointer.getId()));
            bsonWriter.writeEndDocument();

            bsonWriter.writeEndDocument();

        } else if (bsonType.equals(BsonType.JAVASCRIPT)) {

            BsonJavaScript bsonJavaScript = arg.asJavaScript();
            bsonWriter.writeStartDocument();
            bsonWriter.writeName("$code");
            writeString(bsonJavaScript.getCode());
            bsonWriter.writeEndDocument();

        } else if (bsonType.equals(BsonType.SYMBOL)) {

            final BsonSymbol bsonSymbol = arg.asSymbol();
            bsonWriter.writeStartDocument();
            bsonWriter.writeName("$symbol");
            writeString(bsonSymbol.getSymbol());
            bsonWriter.writeEndDocument();

        } else if (bsonType.equals(BsonType.JAVASCRIPT_WITH_SCOPE)) {

            final BsonJavaScriptWithScope bsonJavaScript = arg.asJavaScriptWithScope();
            bsonWriter.writeStartDocument();
            bsonWriter.writeName("$code");
            writeString(bsonJavaScript.getCode());
            bsonWriter.writeName("$scope");
            writeValue(bsonJavaScript.getScope());
            bsonWriter.writeEndDocument();

        } else if (bsonType.equals(BsonType.INT32)) {

            BsonInt32 int32 = arg.asInt32();
            writeInt32(int32.getValue());

        } else if (bsonType.equals(BsonType.TIMESTAMP)) {
            BsonTimestamp bsonTimestamp = arg.asTimestamp();

            bsonWriter.writeStartDocument();
            bsonWriter.writeName("$timestamp");

            bsonWriter.writeStartDocument();
            bsonWriter.writeName("t");
            writeInt32(bsonTimestamp.getTime());
            bsonWriter.writeName("i");
            writeInt32(bsonTimestamp.getInc());
            bsonWriter.writeEndDocument();

            bsonWriter.writeEndDocument();

        } else if (bsonType.equals(BsonType.INT64)) {

            BsonInt64 bsonInt64 = arg.asInt64();

            bsonWriter.writeStartDocument();
            bsonWriter.writeName("$numberLong");
            writeInt64(bsonInt64.getValue());
            bsonWriter.writeEndDocument();

        } else if (bsonType.equals(BsonType.UNDEFINED)) {

            bsonWriter.writeStartDocument();
            bsonWriter.writeName("$undefined");
            writeBoolean(true);
            bsonWriter.writeEndDocument();

        } else if (bsonType.equals(BsonType.NULL)) {

            writeNull();

        } else if (decimal128Enabled && bsonType.equals(BsonType.DECIMAL128)) {

            //since Mongo Java Driver 3.4
            bsonWriter.writeStartDocument();
            bsonWriter.writeName("$numberDecimal");
            writeString(String.valueOf(arg.asDecimal128().getValue()));
            bsonWriter.writeEndDocument();
        }
//        BsonType.DOCUMENT //taken care of in Bson
//        BsonType.ARRAY //taken care of in collection
//        BsonType.END_OF_DOCUMENT //do nothing

    }

    private Map<String, ?> getBsonKeyValueMap(Bson bson) {
        if (bson instanceof BasicDBObject) {
            return (BasicDBObject) bson;
        } else if (bson instanceof BsonDocument) {
            return (BsonDocument) bson;
        } else if (bson instanceof Document) {
            return (Document) bson;
        } else {
            logger.debug("bson KV is null {} ", bson.getClass().getName());
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

    private String binaryAbbreviationForMongo(BsonBinary bsonBinary) {

        final byte[] binary = bsonBinary.getData();
        final int binaryLength = binary.length;
        final Base64.Encoder encoder = Base64.getEncoder().withoutPadding();
        if (binaryLength > DEFAULT_ABBREVIATE_MAX_WIDTH) {
            byte[] limitedBytes = Arrays.copyOf(binary, DEFAULT_ABBREVIATE_MAX_WIDTH);
            StringBuilder buffer = new StringBuilder();
            buffer.append(new String(encoder.encode(limitedBytes), StandardCharsets.ISO_8859_1));
            buffer.append("...(");
            buffer.append(binaryLength);
            buffer.append(")");
            return buffer.toString();
        } else {
            return new String(encoder.encode(binary), StandardCharsets.ISO_8859_1);
        }
    }

    private void arrayAbbreviationForMongo(Object arrayObject) {
        final int length = Array.getLength(arrayObject);
        for (int i = 0; i < length && i < DEFAULT_ABBREVIATE_MAX_WIDTH - 1; i++) {
            writeValue(Array.get(arrayObject, i));
        }
        if (length > DEFAULT_ABBREVIATE_MAX_WIDTH - 2) {
            writeLength(length);
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
            writeLength(length);
        }
    }

    private void writeLength(int length) {
        bsonWriter.writeString("?");
        if (traceBsonBindValue) {
            jsonParameter.add("...(" + length + ")");
        }
    }

    private void writeValue(Object arg) {
        if (arg == null) {
            writeNull();
        } else if (arg instanceof String) {
            writeString((String) arg);
        } else if (isFilter(arg)) {
            parseFilterObject(arg);
        } else if (isUpdates(arg)) {
            parseUpdatesObject(arg);
        } else if (isSort(arg)) {
            parseSortObject(arg);
        } else if (arg.getClass().isArray()) {
            writePrimitiveArrayObject(arg);
        } else if (arg instanceof Collection) {
            writeCollection((Collection<?>) arg);
        } else if (arg instanceof Bson) {
            Bson bson = (Bson) arg;
            writeBsonObject(bson);
        } else if (arg instanceof BsonValue) {
            writeBsonValueObject((BsonValue) arg);
        } else {
            writeRaw(arg);
        }
    }

    private void writeRaw(Object arg) {
        bsonWriter.writeString("?");
        if (traceBsonBindValue) {
            jsonParameter.add(StringUtils.abbreviate(String.valueOf(arg)));
        }
    }

    private void writeString(String string) {
        bsonWriter.writeString("?");
        if (traceBsonBindValue) {
            jsonParameter.add("\"" + StringUtils.abbreviate(StringUtils.replace(string, "\"", "\"\"")) + "\"");
        }
    }

    private void writeInt32(int int32) {
        bsonWriter.writeString("?");
        if (traceBsonBindValue) {
            jsonParameter.add(Integer.toString(int32));
        }
    }

    private void writeInt64(long int64) {
        bsonWriter.writeString("?");
        if (traceBsonBindValue) {
            jsonParameter.add(Long.toString(int64));
        }
    }

    private void writeDouble(double doubleValue) {
        bsonWriter.writeString("?");
        if (traceBsonBindValue) {
            jsonParameter.add(Double.toString(doubleValue));
        }
    }

    private void writeBoolean(boolean boolValue) {
        bsonWriter.writeString("?");
        if (traceBsonBindValue) {
            jsonParameter.add(Boolean.toString(boolValue));
        }
    }

    private void writeNull() {
        bsonWriter.writeString("?");
        if (traceBsonBindValue) {
            jsonParameter.add("null");
        }
    }

    private boolean isSort(Object arg) {
        return MongoConstants.MONGO_SORT_COMPOSITE.equals(arg.getClass().getName());
    }

    private boolean isUpdates(Object arg) {
        String name = arg.getClass().getName();
        return contains(MongoConstants.UPDATES_LIST, name);
    }

    private boolean isFilter(Object arg) {
        String name = arg.getClass().getName();
        return contains(MongoConstants.FILTER_LIST, name);
    }

    private static boolean contains(String[] list, String name) {
        for (String className : list) {
            if (className.equals(name)) {
                return true;
            }
        }
        return false;
    }


}

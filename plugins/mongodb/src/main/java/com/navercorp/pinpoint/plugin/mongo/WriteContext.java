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
import com.navercorp.pinpoint.common.util.Assert;
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
import java.util.Collection;
import java.util.List;
import java.util.Map;

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

    private static final int DEFAULT_ABBREVIATE_MAX_WIDTH = 8;
    static final String UNTRACED = "Unsupported-trace";

    public WriteContext(List<String> jsonParameterAppender, boolean decimal128Enabled, boolean traceBsonBindValue) {
        this.jsonParameter = Assert.requireNonNull(jsonParameterAppender, "jsonParameterAppender");
        this.decimal128Enabled = decimal128Enabled;
        this.traceBsonBindValue = traceBsonBindValue;
    }

    public String parse(Object arg) {
        if (arg instanceof Bson) {
            writeValue(arg);
        } else if (arg instanceof List) {

            if (((List) arg).get(0) instanceof Bson) {
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
        if (argName.equals(MongoConstants.MONGO_UPDATES_SIMPLE)) {
            logger.debug("writing SimpleUpdate");

            bsonWriter.writeStartDocument();
            bsonWriter.writeName(((OperatorGetter) arg)._$PINPOINT$_getOperator());

            bsonWriter.writeStartDocument();
            bsonWriter.writeName(((FieldNameGetter) arg)._$PINPOINT$_getFieldName());
            writeValue(((ValueGetter) arg)._$PINPOINT$_getValue());
            bsonWriter.writeEndDocument();

            bsonWriter.writeEndDocument();
        }
        //WithEachUpdate
        if (argName.equals(MongoConstants.MONGO_UPDATES_WITHEACH)) {
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
        }
        //PushUpdate
        if (argName.equals(MongoConstants.MONGO_UPDATES_PUSH)) {
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
                writeValue(options.getSortDocument());
            }
            bsonWriter.writeEndDocument();
            bsonWriter.writeEndDocument();
            bsonWriter.writeEndDocument();
        }
        //PullAllUpdate
        if (argName.equals(MongoConstants.MONGO_UPDATES_PULLALL)) {
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
        }
        //CompositeUpdate
        if (argName.equals(MongoConstants.MONGO_UPDATES_COMPOSITE)) {
            logger.debug("writing CompositeUpdate");

            bsonWriter.writeStartDocument();

            bsonWriter.writeStartArray("$updates");
            for (Bson value : ((ExtendedBsonListGetter) arg)._$PINPOINT$_getExtendedBsonList()) {
                writeValue(value);
            }
            bsonWriter.writeEndArray();
            bsonWriter.writeEndDocument();
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
                writeValue(value);
            }
            bsonWriter.writeEndArray();
            bsonWriter.writeEndDocument();
        }
    }

    private void parseFilterObject(Object arg) {

        String argName = arg.getClass().getName();
        logger.debug("filter arg : " + arg.getClass().getName());

        //OperatorFilter
        if (argName.equals(MongoConstants.MONGO_FILTER_GEOMETRYOPERATOR)) {
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
        }
        //OperatorFilter
        if (argName.equals(MongoConstants.MONGO_FILTER_OPERATOR)) {
            logger.debug("writing OperatorFilter");

            bsonWriter.writeStartDocument();
            bsonWriter.writeName(((FieldNameGetter) arg)._$PINPOINT$_getFieldName());
            bsonWriter.writeStartDocument();
            bsonWriter.writeName(((OperatorNameGetter) arg)._$PINPOINT$_getOperatorName());
            writeValue(((ValueGetter) arg)._$PINPOINT$_getValue());
            bsonWriter.writeEndDocument();
            bsonWriter.writeEndDocument();
        }
        //IterableOperatorFilter
        else if (argName.equals(MongoConstants.MONGO_FILTER_ITERABLEOPERATOR)) {
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
        }
        //SimpleEncodingFilter
        else if (argName.equals(MongoConstants.MONGO_FILTER_SIMPLEENCODING)) {
            //else if (arg instanceof FieldNameGetter && arg instanceof ValueGetter) {

            logger.debug("writing SimpleEncodingFilter");

            bsonWriter.writeStartDocument();
            bsonWriter.writeName(((FieldNameGetter) arg)._$PINPOINT$_getFieldName());
            writeValue(((ValueGetter) arg)._$PINPOINT$_getValue());
            bsonWriter.writeEndDocument();
        }

        //SimpleFilter
        else if (argName.equals(MongoConstants.MONGO_FILTER_SIMPLE)) {

            logger.debug("writing SimpleFilter");

            bsonWriter.writeStartDocument();
            bsonWriter.writeName(((FieldNameGetter) arg)._$PINPOINT$_getFieldName());
            writeValue(((BsonValueGetter) arg)._$PINPOINT$_getValue());
            bsonWriter.writeEndDocument();
        }

        //AndFilter
        else if (argName.equals(MongoConstants.MONGO_FILTER_AND)) {
            logger.debug("writing AndFilter");

            bsonWriter.writeStartDocument();

            bsonWriter.writeName("$and");
            bsonWriter.writeStartArray();
            for (Bson bsonFilter : ((FiltersGetter) arg)._$PINPOINT$_getFilters()) {
                logger.debug("writing filters");
                writeValue(bsonFilter);
            }
            bsonWriter.writeEndArray();
            bsonWriter.writeEndDocument();
        }

        //NotFilter
        else if (argName.equals(MongoConstants.MONGO_FILTER_NOT)) {
            logger.debug("writing NotFilter");

            bsonWriter.writeStartDocument();

            bsonWriter.writeName("$not");
            writeValue(((FilterGetter) arg)._$PINPOINT$_getFilter());
            bsonWriter.writeEndDocument();
        }

        //TextFilter
        else if (argName.equals(MongoConstants.MONGO_FILTER_TEXT)) {
            logger.debug("writing TextFilter");

            bsonWriter.writeStartDocument();
            bsonWriter.writeName("$text");
            bsonWriter.writeStartDocument();
            bsonWriter.writeName("$search");
            writeValue(((SearchGetter) arg)._$PINPOINT$_getSearch());

            TextSearchOptions textSearchOptions = ((TextSearchOptionsGetter) arg)._$PINPOINT$_getTextSearchOptions();
            if (textSearchOptions.getLanguage() != null) {
                bsonWriter.writeName("$language");
                writeValue(textSearchOptions.getLanguage());
            }
            if (textSearchOptions.getCaseSensitive() != null) {
                bsonWriter.writeName("$caseSensitive");
                writeValue(textSearchOptions.getCaseSensitive());
            }
            if (textSearchOptions.getDiacriticSensitive() != null) {
                bsonWriter.writeName("$diacriticSensitive");
                writeValue(textSearchOptions.getDiacriticSensitive());
            }

            bsonWriter.writeEndDocument();
            bsonWriter.writeEndDocument();
        }

        //OrNorFilter
        else if (argName.equals(MongoConstants.MONGO_FILTER_ORNOR)) {
            logger.debug("writing OrNorFilter");
            String input = setInput(arg);

            bsonWriter.writeStartDocument();
            bsonWriter.writeName(input);
            bsonWriter.writeStartArray();
            for (Bson bsonFilter : ((FiltersGetter) arg)._$PINPOINT$_getFilters()) {
                writeValue(bsonFilter);
            }
            bsonWriter.writeEndArray();
            bsonWriter.writeEndDocument();
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

            String abbreviatedBinary = binaryAbbreviationForMongo(arg);
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

    private Map<String, ?> getBsonKeyValueMap(Object bson) {
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

    private String binaryAbbreviationForMongo(BsonValue arg) {

        final byte[] binary = arg.asBinary().getData();
        final int binaryLength = binary.length;

        if (binaryLength > DEFAULT_ABBREVIATE_MAX_WIDTH) {
            return Base64.encode(binary, 0, DEFAULT_ABBREVIATE_MAX_WIDTH) + "...(" + binaryLength + ")";
        } else {
            return Base64.encode(binary);
        }
    }

    private void arrayAbbreviationForMongo(Object arg) {
        final int length = Array.getLength(arg);
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
        } else if (isFilter(arg)) {
            parseFilterObject(arg);
        } else if (isUpdates(arg)) {
            parseUpdatesObject(arg);
        } else if (isSort(arg)) {
            parseSortObject(arg);
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

    private boolean isSort(Object arg) {
        if (MongoConstants.MONGO_SORT_COMPOSITE.equals(arg.getClass().getName())) {
            return true;
        }
        return false;
    }

    private boolean isUpdates(Object arg) {
        if (MongoConstants.UPDATESLIST.contains(arg.getClass().getName())) {
            return true;
        }
        return false;
    }

    private boolean isFilter(Object arg) {
        if (MongoConstants.FILTERLIST.contains(arg.getClass().getName())) {
            return true;
        }
        return false;
    }
}

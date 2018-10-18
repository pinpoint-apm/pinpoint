/*
 * Copyright 2018 NAVER Corp.
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

package com.navercorp.pinpoint.plugin.mongo.interceptor;

import com.mongodb.BasicDBObject;
import com.mongodb.MongoClient;
import com.mongodb.WriteConcern;
import com.mongodb.client.MongoCollection;
import com.navercorp.pinpoint.bootstrap.context.DatabaseInfo;
import com.navercorp.pinpoint.bootstrap.context.MethodDescriptor;
import com.navercorp.pinpoint.bootstrap.context.ParsingResult;
import com.navercorp.pinpoint.bootstrap.context.SpanEventRecorder;
import com.navercorp.pinpoint.bootstrap.context.TraceContext;
import com.navercorp.pinpoint.bootstrap.interceptor.SpanEventSimpleAroundInterceptorForPlugin;
import com.navercorp.pinpoint.bootstrap.logging.PLogger;
import com.navercorp.pinpoint.bootstrap.logging.PLoggerFactory;
import com.navercorp.pinpoint.bootstrap.plugin.jdbc.DatabaseInfoAccessor;
import com.navercorp.pinpoint.bootstrap.plugin.jdbc.MongoDatabaseInfo;
import com.navercorp.pinpoint.bootstrap.plugin.jdbc.ParsingResultAccessor;
import com.navercorp.pinpoint.bootstrap.plugin.jdbc.UnKnownDatabaseInfo;
import com.navercorp.pinpoint.bootstrap.util.InterceptorUtils;
import org.bson.BsonDocument;
import org.bson.Document;
import org.bson.conversions.Bson;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

/**
 * @author Roy Kim
 */
public class MongoCUDSessionInterceptor extends SpanEventSimpleAroundInterceptorForPlugin {

    private final PLogger logger = PLoggerFactory.getLogger(this.getClass());
    private final boolean isDebug = logger.isDebugEnabled();
    private final boolean collectJson;

    public MongoCUDSessionInterceptor(TraceContext traceContext, MethodDescriptor descriptor, boolean collectJson) {
        super(traceContext, descriptor);
        this.collectJson = collectJson;
    }

    @Override
    protected void doInBeforeTrace(SpanEventRecorder recorder, Object target, Object[] args) {

        DatabaseInfo databaseInfo;
        if (target instanceof DatabaseInfoAccessor) {
            databaseInfo = ((DatabaseInfoAccessor) target)._$PINPOINT$_getDatabaseInfo();
        } else {
            databaseInfo = null;
        }

        if (databaseInfo == null) {
            databaseInfo = UnKnownDatabaseInfo.INSTANCE;
        }

        recorder.recordServiceType(databaseInfo.getExecuteQueryType());
        recorder.recordEndPoint(databaseInfo.getMultipleHost());
        recorder.recordDestinationId(databaseInfo.getDatabaseId());

        recorder.recordApi(methodDescriptor);
        recorder.recordMongoCollectionInfo(((MongoDatabaseInfo) databaseInfo).getCollectionName(), ((MongoDatabaseInfo) databaseInfo).getWriteConcern());
    }

    @Override
    protected void prepareAfterTrace(Object target, Object[] args, Object result, Throwable throwable) {

        if(collectJson) {
            final boolean success = InterceptorUtils.isSuccess(throwable);
            if (success) {
                if(args != null) {
                    StringBuilder bson = new StringBuilder();
                    ParsingResult parsingResult;

                    for(Object arg : args) {

                        if(bson.length() != 0){
                            bson.append(", ");
                        }

                        if (arg instanceof Bson) {
                            if(arg instanceof BasicDBObject) {
                                bson.append(((BasicDBObject)arg).toJson());
                            }else if(arg instanceof BsonDocument) {
                                bson.append(((BsonDocument)arg).toJson());
                            }else if(arg instanceof Document) {
                                bson.append(((Document)arg).toJson());
                            }else{
                                bson.append(((Bson) arg).toBsonDocument(BsonDocument.class, MongoClient.getDefaultCodecRegistry()).toJson());
                            }
                            //TODO leave comments for further use
    //                        if(arg instanceof BsonDocumentWrapper) {
    //                            bson.append(arg.toString());
    //                        }
    //                        if(arg instanceof CommandResult) {
    //                            bson.append(arg.toString());
    //                        }
    //                        if(arg instanceof RawBsonDocument) {
    //                            bson.append(arg.toString());
    //                        }
                        }
                    }
                    parsingResult = traceContext.parseJson(bson.toString());

                    if (parsingResult != null) {
                        ((ParsingResultAccessor) target)._$PINPOINT$_setParsingResult(parsingResult);
                    } else {
                        if (logger.isErrorEnabled()) {
                            logger.error("sqlParsing fail. parsingResult is null bson:{}", bson);
                        }
                    }
                }
            }
        }
    }

    @Override
    public void doInAfterTrace(SpanEventRecorder recorder, Object target, Object[] args, Object result, Throwable throwable) {

        if(collectJson) {
            if (args != null) {
                for (Object arg : args) {
                    if (arg instanceof Bson) {
                        ParsingResult parsingResult = ((ParsingResultAccessor) target)._$PINPOINT$_getParsingResult();
                        recorder.recordJsonParsingResult(parsingResult);
                        break;
                    }
                }
            }
        }
        recorder.recordException(throwable);
    }

    public String getWriteConcern0(WriteConcern writeConcern) {

        for (final Field f : WriteConcern.class.getFields()) {
            if (Modifier.isStatic(f.getModifiers()) && f.getType().equals(WriteConcern.class)) {

                try {
                    if(writeConcern.equals(f.get(null))){
                        return f.getName().toUpperCase();
                    }
                } catch (IllegalAccessException e) {
                    //throw new RuntimeException(e);//TODO
                }
            }
        }
        return null;
    }
}

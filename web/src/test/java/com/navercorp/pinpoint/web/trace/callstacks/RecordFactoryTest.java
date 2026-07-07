/*
 * Copyright 2025 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.navercorp.pinpoint.web.trace.callstacks;

import com.navercorp.pinpoint.common.profiler.trace.AnnotationKeyRegistry;
import com.navercorp.pinpoint.common.profiler.trace.TraceMetadataLoader;
import com.navercorp.pinpoint.common.server.bo.AnnotationBo;
import com.navercorp.pinpoint.common.server.bo.ApiMetaDataBo;
import com.navercorp.pinpoint.common.server.bo.ExceptionInfo;
import com.navercorp.pinpoint.common.server.bo.MethodTypeEnum;
import com.navercorp.pinpoint.common.server.bo.SpanBo;
import com.navercorp.pinpoint.common.server.bo.SpanEventBo;
import com.navercorp.pinpoint.common.server.trace.ApiParserProvider;
import com.navercorp.pinpoint.common.server.trace.PinpointServerTraceId;
import com.navercorp.pinpoint.common.server.uid.ServiceUid;
import com.navercorp.pinpoint.common.server.util.ServerTraceMetadataLoaderService;
import com.navercorp.pinpoint.io.SpanVersion;
import com.navercorp.pinpoint.loader.service.AnnotationKeyRegistryService;
import com.navercorp.pinpoint.loader.service.DefaultAnnotationKeyRegistryService;
import com.navercorp.pinpoint.loader.service.ServiceTypeRegistryService;
import com.navercorp.pinpoint.loader.service.TraceMetadataLoaderService;
import com.navercorp.pinpoint.web.component.AnnotationKeyMatcherService;
import com.navercorp.pinpoint.web.trace.service.RecorderFactoryProvider;
import com.navercorp.pinpoint.web.trace.service.TransactionInfoService;
import com.navercorp.pinpoint.web.trace.service.TransactionInfoServiceImpl;
import com.navercorp.pinpoint.web.trace.span.Align;
import com.navercorp.pinpoint.web.trace.span.CallTreeIterator;
import com.navercorp.pinpoint.web.trace.span.CallTreeNode;
import com.navercorp.pinpoint.web.trace.span.SpanAlign;
import com.navercorp.pinpoint.web.trace.span.SpanEventAlign;
import com.navercorp.pinpoint.web.trace.span.SpanFilters;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @author Woonduk Kang(emeroad)
 */
@ExtendWith(MockitoExtension.class)
public class RecordFactoryTest {

    @Mock
    private ServiceTypeRegistryService mockServiceTypeRegistryService;

    @Mock
    private AnnotationKeyRegistryService mockAnnotationKeyRegistryService;

    @Mock
    private AnnotationKeyMatcherService mockAnnotationKeyMatcherService;

    @Mock
    private AnnotationRecordFormatter mockAnnotationRecordFormatter;

    @Mock
    private ApiParserProvider apiParserProvider;

    private RecordFactory newRecordFactory() {
        RecorderFactoryProvider recorderFactoryProvider = new RecorderFactoryProvider(mockServiceTypeRegistryService,
                mockAnnotationKeyMatcherService, mockAnnotationKeyRegistryService, mockAnnotationRecordFormatter, apiParserProvider);
        return recorderFactoryProvider.getRecordFactory();
    }


    @Test
    public void getException_check_argument() {
        final RecordFactory factory = newRecordFactory();

        SpanBo spanBo = new SpanBo();
        spanBo.setTransactionId(new PinpointServerTraceId("test", 0, 0));
        spanBo.setExceptionInfo(new ExceptionInfo(1, null));
        Align align = new SpanAlign(spanBo);


        com.navercorp.pinpoint.web.trace.callstacks.Record exceptionRecord = factory.getException(0, 0, align);

        Assertions.assertNotNull(exceptionRecord.getArguments());
    }


    @Test
    public void getException_otel_blankClassName_fallsBackToError() {
        final RecordFactory factory = newRecordFactory();

        SpanBo spanBo = new SpanBo();
        spanBo.setTransactionId(new PinpointServerTraceId("test", 0, 0));
        // OTel message-only encoding: empty class-name prefix (leading delimiter)
        spanBo.setExceptionInfo(new ExceptionInfo(ExceptionInfo.OTEL_EXCEPTION_ID, ":Connection refused"));
        Align align = new SpanAlign(spanBo, false, true); // openTelemetry = true

        Record exceptionRecord = factory.getException(0, 0, align);

        assertThat(exceptionRecord.getTitle()).isEqualTo("ERROR");
        assertThat(exceptionRecord.getArguments()).isEqualTo("Connection refused");
    }

    @Test
    public void getException_otel_withClassName_usesSimpleName() {
        final RecordFactory factory = newRecordFactory();

        SpanBo spanBo = new SpanBo();
        spanBo.setTransactionId(new PinpointServerTraceId("test", 0, 0));
        spanBo.setExceptionInfo(new ExceptionInfo(ExceptionInfo.OTEL_EXCEPTION_ID, "java.io.IOException:disk full"));
        // exceptionClass is populated by SpanServiceImpl.transitionException from the message prefix
        spanBo.setExceptionClass("java.io.IOException");
        Align align = new SpanAlign(spanBo, false, true); // openTelemetry = true

        Record exceptionRecord = factory.getException(0, 0, align);

        assertThat(exceptionRecord.getTitle()).isEqualTo("IOException");
        assertThat(exceptionRecord.getArguments()).isEqualTo("disk full");
    }

    @Test
    public void getParameter_check_argument() {

        final RecordFactory factory = newRecordFactory();

        com.navercorp.pinpoint.web.trace.callstacks.Record exceptionRecord = factory.getParameter(0, 0, "testMethod", null);

        Assertions.assertEquals("null", exceptionRecord.getArguments());
    }

    // 0 = {parent = null, child = 1 reference, sibling = null
    // , align = SpanAlign{
    //              spanBo = SpanBo { version=1, agentId='express-node-sample-id', agentName='', applicationId='express-node-sample-name', agentStartTime=1670293953108
    //                          , transactionId=TransactionId{agentId='express-node-sample-id', agentStartTime=1670293953108, transactionSequence=30}
    //                          , spanId=8174884636707391, parentSpanId=-1, parentApplicationId='null', parentApplicationServiceType=0, startTime=1670305848569
    //                          , elapsed=14, rpc='/', serviceType=1400, endPoint='localhost:3000', apiId=1
    //                          , annotationBoList=[AnnotationBo{key=46, value=200, isAuthorized=true}
    //                                            , AnnotationBo{key=13, value=ApiMetaDataBo{agentId='express-node-sample-id'
    //                                                          , startTime=1670293953108, apiId=1, apiInfo='Node Server Process'
    //                                                          , lineNumber=0, methodTypeEnum=WEB_REQUEST, location='null'}, isAuthorized=true}
    //                                            , AnnotationBo{key=10015, value=Node Server Process, isAuthorized=true}]
    //                          , flag=0, errCode=0
    //                          , spanEventBoList=[{version=0, sequence=0, startElapsed=0, endElapsed=1, serviceType=6600
    //                                                , destinationId=localhost:3000, endPoint=localhost:3000, apiId=17
    //                                                , annotationBoList=[AnnotationBo{key=-1, value=/, isAuthorized=true}
    //                                                                  , AnnotationBo{key=13, value=ApiMetaDataBo{agentId='express-node-sample-id'
    //                                                                                                           , startTime=1670293953108, apiId=17, apiInfo='express.Function.use(logger)'
    //                                                                                                           , lineNumber=42, methodTypeEnum=DEFAULT, location='/Users/workspace/pinpoint/@pinpoint-naver-apm/pinpoint-agent-node/samples/express/src/app.js'}
    //                                                                               , isAuthorized=true}
    //                                                                  , AnnotationBo{key=12, value=express.Function.use(logger):42, isAuthorized=true}]
    //                                                 , depth=1, nextSpanId=-1, hasException=false, exceptionClass=null, nextAsyncId=0}
    //                                           , {version=0, sequence=1, startElapsed=1, endElapsed=0, serviceType=6600
    //                                                , destinationId=localhost:3000, endPoint=localhost:3000, apiId=18
    //                                                , annotationBoList=[AnnotationBo{key=-1, value=/, isAuthorized=true}
    //                                                                  , AnnotationBo{key=13, value=ApiMetaDataBo{agentId='express-node-sample-id', startTime=1670293953108, apiId=18, apiInfo='express.Function.use(jsonParser)', lineNumber=43, methodTypeEnum=DEFAULT, location='/Users/workspace/pinpoint/@pinpoint-naver-apm/pinpoint-agent-node/samples/express/src/app.js'}, isAuthorized=true}
    //                                                                  , AnnotationBo{key=12, value=express.Function.use(jsonParser):43, isAuthorized=true}]
    //                                                                  , depth=1, nextSpanId=-1, hasException=false, exceptionClass=null, nextAsyncId=0}
    //                                           , {version=0, sequence=2, startElapsed=1, endElapsed=0, serviceType=6600
    //                                                , destinationId=localhost:3000, endPoint=localhost:3000, apiId=19
    //                                                , annotationBoList=[AnnotationBo{key=-1, value=/, isAuthorized=true}
    //                                                                  , AnnotationBo{key=13, value=ApiMetaDataBo{agentId='express-node-sample-id', startTime=1670293953108, apiId=19, apiInfo='express.Function.use(urlencodedParser)', lineNumber=44, methodTypeEnum=DEFAULT, location='/Users/workspace/pinpoint/@pinpoint-naver-apm/pinpoint-agent-node/samples/express/src/app.js'}, isAuthorized=true}
    //                                                                  , AnnotationBo{key=12, value=express.Function.use(urlencodedParser):44, isAuthorized=true}]
    //                                                , depth=1, nextSpanId=-1, hasException=false, exceptionClass=null, nextAsyncId=0}
    //                                           , {version=0, sequence=3, startElapsed=1, endElapsed=0, serviceType=6600
    //                                                 , destinationId=localhost:3000, endPoint=localhost:3000, apiId=20
    //                                                 , annotationBoList=[AnnotationBo{key=-1, value=/, isAuthorized=true}
    //                                                                     , AnnotationBo{key=13, value=ApiMetaDataBo{agentId='express-node-sample-id'
    //                                                                                      , startTime=1670293953108, apiId=20
    //                                                                                      , apiInfo='express.Function.use(cookieParser)'
    //                                                                                      , lineNumber=45, methodTypeEnum=DEFAULT
    //                                                                                      , location='/Users/workspace/pinpoint/@pinpoint-naver-apm/pinpoint-agent-node/samples/express/src/app.js'}
    //                                                                             , isAuthorized=true}
    //                                                                     , AnnotationBo{key=12, value=express.Function.use(cookieParser):45, isAuthorized=true}]
    //                                                , depth=1, nextSpanId=-1, hasException=false, exceptionClass=null, nextAsyncId=0}
    //                                           , {version=0, sequence=4, startElapsed=1, endElapsed=0, serviceType=6600
    //                                               , destinationId=localhost:3000, endPoint=localhost:3000, apiId=21
    //                                               , annotationBoList=[AnnotationBo{key=-1, value=/, isAuthorized=true}
    //                                                                   , AnnotationBo{key=13, value=ApiMetaDataBo{agentId='express-node-sample-id'
    //                                                                                                              , startTime=1670293953108, apiId=21, apiInfo='express.Function.use(serveStatic)'
    //                                                                                                              , lineNumber=46, methodTypeEnum=DEFAULT
    //                                                                                                              , location='/Users/workspace/pinpoint/@pinpoint-naver-apm/pinpoint-agent-node/samples/express/src/app.js'}
    //                                                                                   , isAuthorized=true}
    //                                                                   , AnnotationBo{key=12, value=express.Function.use(serveStatic):46, isAuthorized=true}]
    //                                                , depth=1, nextSpanId=-1, hasException=false, exceptionClass=null, nextAsyncId=0}
    //                                            , {version=0, sequence=5, startElapsed=1, endElapsed=1, serviceType=6600, destinationId=localhost:3000, endPoint=localhost:3000, apiId=11, annotationBoList=[AnnotationBo{key=-1, value=/, isAuthorized=true}, AnnotationBo{key=13, value=ApiMetaDataBo{agentId='express-node-sample-id', startTime=1670293953108, apiId=11, apiInfo='express.Function.proto.get(path, callback)', lineNumber=24, methodTypeEnum=DEFAULT, location='/Users/workspace/pinpoint/@pinpoint-naver-apm/pinpoint-agent-node/samples/express/src/routes/index.js'}, isAuthorized=true}, AnnotationBo{key=12, value=express.Function.proto.get(path, callback):24, isAuthorized=true}], depth=1, nextSpanId=-1, hasException=false, exceptionClass=null, nextAsyncId=0}
    //                                            , {version=0, sequence=6, startElapsed=1, endElapsed=0, serviceType=8200, destinationId=Redis, endPoint=127.0.0.1:6379, apiId=0, annotationBoList=[AnnotationBo{key=12, value=redis.set.call, isAuthorized=true}, AnnotationBo{key=13, value=ApiMetaDataBo{agentId='express-node-sample-id', startTime=1670305848570, apiId=0, apiInfo='redis.set.call', lineNumber=0, methodTypeEnum=DEFAULT, location='null'}, isAuthorized=true}], depth=2, nextSpanId=-1, hasException=false, exceptionClass=null, nextAsyncId=57}
    //                                            , {version=0, sequence=7, startElapsed=1, endElapsed=0, serviceType=8200, destinationId=Redis, endPoint=127.0.0.1:6379, apiId=0, annotationBoList=[AnnotationBo{key=12, value=redis.get.call, isAuthorized=true}, AnnotationBo{key=13, value=ApiMetaDataBo{agentId='express-node-sample-id', startTime=1670305848570, apiId=0, apiInfo='redis.get.call', lineNumber=0, methodTypeEnum=DEFAULT, location='null'}, isAuthorized=true}], depth=2, nextSpanId=-1, hasException=false, exceptionClass=null, nextAsyncId=58}], spanChunkBoList=[SpanChunkBo{version=1, agentId='express-node-sample-id', applicationId='express-node-sample-name', agentStartTime=1670293953108, transactionId=TransactionId{agentId='express-node-sample-id', agentStartTime=1670293953108, transactionSequence=30}, spanId=8174884636707391, endPoint='null', serviceType=0, applicationServiceType=null, spanEventBoList=[{version=0, sequence=0, startElapsed=0, endElapsed=0, serviceType=100, destinationId=null, endPoint=null, apiId=2, annotationBoList=[AnnotationBo{key=13, value=ApiMetaDataBo{agentId='express-node-sample-id', startTime=1670293953108, apiId=2, apiInfo='Asynchronous Invocation', lineNumber=0, methodTypeEnum=INVOCATION, location='null'}, isAuthorized=true}, AnnotationBo{key=10015, value=Asynchronous Invocation, isAuthorized=true}], depth=1, nextSpanId=-1, hasException=false, exceptionClass=null, nextAsyncId=0}
    //                                            , {version=0, sequence=1, startElapsed=0, endElapsed=0, serviceType=8200, destinationId=Redis, endPoint=127.0.0.1:6379, apiId=0, annotationBoList=[AnnotationBo{key=12, value=redis.set.end, isAuthorized=true}, AnnotationBo{key=13, value=ApiMetaDataBo{agentId='express-node-sample-id', startTime=1670305848570, apiId=0, apiInfo='redis.set.end', lineNumber=0, methodTypeEnum=DEFAULT, location='null'}, isAuthorized=true}], depth=2, nextSpanId=8204265135595103, hasException=false, exceptionClass=null, nextAsyncId=0}], collectorAcceptTime=1670305848574, localAsyncId=LocalAsyncIdBo{asyncId=57, sequence=0}, keyTIme=1670305848570}, SpanChunkBo{version=1, agentId='express-node-sample-id', applicationId='express-node-sample-name', agentStartTime=1670293953108, transactionId=TransactionId{agentId='express-node-sample-id', agentStartTime=1670293953108, transactionSequence=30}, spanId=8174884636707391, endPoint='null', serviceType=0, applicationServiceType=null, spanEventBoList=[{version=0, sequence=0, startElapsed=0, endElapsed=0, serviceType=100, destinationId=null, endPoint=null, apiId=2, annotationBoList=[AnnotationBo{key=13, value=ApiMetaDataBo{agentId='express-node-sample-id', startTime=1670293953108, apiId=2, apiInfo='Asynchronous Invocation', lineNumber=0, methodTypeEnum=INVOCATION, location='null'}, isAuthorized=true}, AnnotationBo{key=10015, value=Asynchronous Invocation, isAuthorized=true}], depth=1, nextSpanId=-1, hasException=false, exceptionClass=null, nextAsyncId=0}
    //                                            , {version=0, sequence=1, startElapsed=0, endElapsed=0, serviceType=8200, destinationId=Redis, endPoint=127.0.0.1:6379, apiId=0, annotationBoList=[AnnotationBo{key=12, value=redis.get.end, isAuthorized=true}, AnnotationBo{key=13, value=ApiMetaDataBo{agentId='express-node-sample-id', startTime=1670305848570, apiId=0, apiInfo='redis.get.end', lineNumber=0, methodTypeEnum=DEFAULT, location='null'}, isAuthorized=true}], depth=2, nextSpanId=1376599456185043, hasException=false, exceptionClass=null, nextAsyncId=0}], collectorAcceptTime=1670305848574, localAsyncId=LocalAsyncIdBo{asyncId=58, sequence=0}, keyTIme=1670305848570}]
    //                          , collectorAcceptTime=1670305848586, hasException=false, exceptionClass='null', applicationServiceType=1400, acceptorHost='null', remoteAddr='::1', loggingTransactionInfo=0
    //              }
    //              , hasChild=true, meta=false, id=0, gap=0, depth=0, executionMilliseconds=12
    //           }
    //   }
    // 1 = {parent = 0 reference, child = null, sibling = 2 reference
    //     , align = SpanEventAlign{id = 0, gap = 0, depth = 1, executionMilliseconds = 1
    //                              , spanBo = SpanBo{version=1, agentId='express-node-sample-id', agentName='', applicationId='express-node-sample-name'
    //                                                 , agentStartTime=1670293953108, transactionId=TransactionId{agentId='express-node-sample-id', agentStartTime=1670293953108, transactionSequence=30}
    //                                                 , spanId=8174884636707391, parentSpanId=-1, parentApplicationId='null', parentApplicationServiceType=0
    //                                                 , startTime=1670305848569, elapsed=14, rpc='/', serviceType=1400, endPoint='localhost:3000', apiId=1
    //                                                 , annotationBoList=[AnnotationBo{key=46, value=200, isAuthorized=true}
    //                                                                   , AnnotationBo{key=13, value=ApiMetaDataBo{agentId='express-node-sample-id', startTime=1670293953108, apiId=1, apiInfo='Node Server Process', lineNumber=0, methodTypeEnum=WEB_REQUEST, location='null'}, isAuthorized=true}
    //                                                                   , AnnotationBo{key=10015, value=Node Server Process, isAuthorized=true}]
    //                                                 , flag=0, errCode=0
    //                                                 , spanEventBoList=[{version=0, sequence=0, startElapsed=0, endElapsed=1, serviceType=6600
    //                                                                       , destinationId=localhost:3000, endPoint=localhost:3000, apiId=17
    //                                                                       , annotationBoList=[AnnotationBo{key=-1, value=/, isAuthorized=true}
    //                                                                                              , AnnotationBo{key=13, value=ApiMetaDataBo{agentId='express-node-sample-id', startTime=1670293953108, apiId=17, apiInfo='express.Function.use(logger)', lineNumber=42, methodTypeEnum=DEFAULT, location='/Users/workspace/pinpoint/@pinpoint-naver-apm/pinpoint-agent-node/samples/express/src/app.js'}, isAuthorized=true}
    //                                                                                              , AnnotationBo{key=12, value=express.Function.use(logger):42, isAuthorized=true}], depth=1, nextSpanId=-1, hasException=false, exceptionClass=null, nextAsyncId=0}
    //                                                                    , {version=0, sequence=1, startElapsed=1, endElapsed=0, serviceType=6600, destinationId=localhost:3000, endPoint=localhost:3000, apiId=18
    //                                                                          , annotationBoList=[AnnotationBo{key=-1, value=/, isAuthorized=true}, AnnotationBo{key=13, value=ApiMetaDataBo{agentId='express-node-sample-id', startTime=1670293953108, apiId=18, apiInfo='express.Function.use(jsonParser)', lineNumber=43, methodTypeEnum=DEFAULT, location='/Users/workspace/pinpoint/@pinpoint-naver-apm/pinpoint-agent-node/samples/express/src/app.js'}, isAuthorized=true}, AnnotationBo{key=12, value=express.Function.use(jsonParser):43, isAuthorized=true}], depth=1, nextSpanId=-1, hasException=false, exceptionClass=null, nextAsyncId=0}, {version=0, sequence=2, startElapsed=1, endElapsed=0, serviceType=6600, destinationId=localhost:3000, endPoint=localhost:3000, apiId=19, annotationBoList=[AnnotationBo{key=-1, value=/, isAuthorized=true}, AnnotationBo{key=13, value=ApiMetaDataBo{agentId='express-node-sample-id', startTime=1670293953108, apiId=19, apiInfo='express.Function.use(urlencodedParser)', lineNumber=44, methodTypeEnum=DEFAULT, location='/Users/workspace/pinpoint/@pinpoint-naver-apm/pinpoint-agent-node/samples/express/src/app.js'}, isAuthorized=true}, AnnotationBo{key=12, value=express.Function.use(urlencodedParser):44, isAuthorized=true}], depth=1, nextSpanId=-1, hasException=false, exceptionClass=null, nextAsyncId=0}, {version=0, sequence=3, startElapsed=1, endElapsed=0, serviceType=6600, destinationId=localhost:3000, endPoint=localhost:3000, apiId=20, annotationBoList=[AnnotationBo{key=-1, value=/, isAuthorized=true}, AnnotationBo{key=13, value=ApiMetaDataBo{agentId='express-node-sample-id', startTime=1670293953108, apiId=20, apiInfo='express.Function.use(cookieParser)', lineNumber=45, methodTypeEnum=DEFAULT, location='/Users/workspace/pinpoint/@pinpoint-naver-apm/pinpoint-agent-node/samples/express/src/app.js'}, isAuthorized=true}, AnnotationBo{key=12, value=express.Function.use(cookieParser):45, isAuthorized=true}], depth=1, nextSpanId=-1, hasException=false, exceptionClass=null, nextAsyncId=0}, {version=0, sequence=4, startElapsed=1, endElapsed=0, serviceType=6600, destinationId=localhost:3000, endPoint=localhost:3000, apiId=21, annotationBoList=[AnnotationBo{key=-1, value=/, isAuthorized=true}, AnnotationBo{key=13, value=ApiMetaDataBo{agentId='express-node-sample-id', startTime=1670293953108, apiId=21, apiInfo='express.Function.use(serveStatic)', lineNumber=46, methodTypeEnum=DEFAULT, location='/Users/workspace/pinpoint/@pinpoint-naver-apm/pinpoint-agent-node/samples/express/src/app.js'}, isAuthorized=true}, AnnotationBo{key=12, value=express.Function.use(serveStatic):46, isAuthorized=true}], depth=1, nextSpanId=-1, hasException=false, exceptionClass=null, nextAsyncId=0}, {version=0, sequence=5, startElapsed=1, endElapsed=1, serviceType=6600, destinationId=localhost:3000, endPoint=localhost:3000, apiId=11, annotationBoList=[AnnotationBo{key=-1, value=/, isAuthorized=true}, AnnotationBo{key=13, value=ApiMetaDataBo{agentId='express-node-sample-id', startTime=1670293953108, apiId=11, apiInfo='express.Function.proto.get(path, callback)', lineNumber=24, methodTypeEnum=DEFAULT, location='/Users/workspace/pinpoint/@pinpoint-naver-apm/pinpoint-agent-node/samples/express/src/routes/index.js'}, isAuthorized=true}, AnnotationBo{key=12, value=express.Function.proto.get(path, callback):24, isAuthorized=true}], depth=1, nextSpanId=-1, hasException=false, exceptionClass=null, nextAsyncId=0}, {version=0, sequence=6, startElapsed=1, endElapsed=0, serviceType=8200, destinationId=Redis, endPoint=127.0.0.1:6379, apiId=0, annotationBoList=[AnnotationBo{key=12, value=redis.set.call, isAuthorized=true}, AnnotationBo{key=13, value=ApiMetaDataBo{agentId='express-node-sample-id', startTime=1670305848570, apiId=0, apiInfo='redis.set.call', lineNumber=0, methodTypeEnum=DEFAULT, location='null'}, isAuthorized=true}], depth=2, nextSpanId=-1, hasException=false, exceptionClass=null, nextAsyncId=57}, {version=0, sequence=7, startElapsed=1, endElapsed=0, serviceType=8200, destinationId=Redis, endPoint=127.0.0.1:6379, apiId=0, annotationBoList=[AnnotationBo{key=12, value=redis.get.call, isAuthorized=true}, AnnotationBo{key=13, value=ApiMetaDataBo{agentId='express-node-sample-id', startTime=1670305848570, apiId=0, apiInfo='redis.get.call', lineNumber=0, methodTypeEnum=DEFAULT, location='null'}, isAuthorized=true}], depth=2, nextSpanId=-1, hasException=false, exceptionClass=null, nextAsyncId=58}], spanChunkBoList=[SpanChunkBo{version=1, agentId='express-node-sample-id', applicationId='express-node-sample-name', agentStartTime=1670293953108, transactionId=TransactionId{agentId='express-node-sample-id', agentStartTime=1670293953108, transactionSequence=30}, spanId=8174884636707391, endPoint='null', serviceType=0, applicationServiceType=null, spanEventBoList=[{version=0, sequence=0, startElapsed=0, endElapsed=0, serviceType=100, destinationId=null, endPoint=null, apiId=2, annotationBoList=[AnnotationBo{key=13, value=ApiMetaDataBo{agentId='express-node-sample-id', startTime=1670293953108, apiId=2, apiInfo='Asynchronous Invocation', lineNumber=0, methodTypeEnum=INVOCATION, location='null'}, isAuthorized=true}, AnnotationBo{key=10015, value=Asynchronous Invocation, isAuthorized=true}], depth=1, nextSpanId=-1, hasException=false, exceptionClass=null, nextAsyncId=0}, {version=0, sequence=1, startElapsed=0, endElapsed=0, serviceType=8200, destinationId=Redis, endPoint=127.0.0.1:6379, apiId=0, annotationBoList=[AnnotationBo{key=12, value=redis.set.end, isAuthorized=true}, AnnotationBo{key=13, value=ApiMetaDataBo{agentId='express-node-sample-id', startTime=1670305848570, apiId=0, apiInfo='redis.set.end', lineNumber=0, methodTypeEnum=DEFAULT, location='null'}, isAuthorized=true}], depth=2, nextSpanId=8204265135595103, hasException=false, exceptionClass=null, nextAsyncId=0}], collectorAcceptTime=1670305848574, localAsyncId=LocalAsyncIdBo{asyncId=57, sequence=0}, keyTIme=1670305848570}, SpanChunkBo{version=1, agentId='express-node-sample-id', applicationId='express-node-sample-name', agentStartTime=1670293953108, transactionId=TransactionId{agentId='express-node-sample-id', agentStartTime=1670293953108, transactionSequence=30}, spanId=8174884636707391, endPoint='null', serviceType=0, applicationServiceType=null, spanEventBoList=[{version=0, sequence=0, startElapsed=0, endElapsed=0, serviceType=100, destinationId=null, endPoint=null, apiId=2, annotationBoList=[AnnotationBo{key=13, value=ApiMetaDataBo{agentId='express-node-sample-id', startTime=1670293953108, apiId=2, apiInfo='Asynchronous Invocation', lineNumber=0, methodTypeEnum=INVOCATION, location='null'}, isAuthorized=true}, AnnotationBo{key=10015, value=Asynchronous Invocation, isAuthorized=true}], depth=1, nextSpanId=-1, hasException=false, exceptionClass=null, nextAsyncId=0}, {version=0, sequence=1, startElapsed=0, endElapsed=0, serviceType=8200, destinationId=Redis, endPoint=127.0.0.1:6379, apiId=0, annotationBoList=[AnnotationBo{key=12, value=redis.get.end, isAuthorized=true}, AnnotationBo{key=13, value=ApiMetaDataBo{agentId='express-node-sample-id', startTime=1670305848570, apiId=0, apiInfo='redis.get.end', lineNumber=0, methodTypeEnum=DEFAULT, location='null'}, isAuthorized=true}], depth=2, nextSpanId=1376599456185043, hasException=false, exceptionClass=null, nextAsyncId=0}], collectorAcceptTime=1670305848574, localAsyncId=LocalAsyncIdBo{asyncId=58, sequence=0}, keyTIme=1670305848570}], collectorAcceptTime=1670305848586, hasException=false, exceptionClass='null', applicationServiceType=1400, acceptorHost='null', remoteAddr='::1', loggingTransactionInfo=0}, spanEventBo = {version=0, sequence=0, startElapsed=0, endElapsed=1, serviceType=6600, destinationId=localhost:3000, endPoint=localhost:3000, apiId=17, annotationBoList=[AnnotationBo{key=-1, value=/, isAuthorized=true}, AnnotationBo{key=13, value=ApiMetaDataBo{agentId='express-node-sample-id', startTime=1670293953108, apiId=17, apiInfo='express.Function.use(logger)', lineNumber=42, methodTypeEnum=DEFAULT, location='/Users/workspace/pinpoint/@pinpoint-naver-apm/pinpoint-agent-node/samples/express/src/app.js'}, isAuthorized=true}, AnnotationBo{key=12, value=express.Function.use(logger):42, isAuthorized=true}]
    //                                                                          , depth=1, nextSpanId=-1, hasException=false, exceptionClass=null, nextAsyncId=0}}}"
    @Test
    public void testMakeRecord() {
        SpanEventBo spanEventBo1 = newSpanEventBo((short) 0, 0, 1, 17, 42,
                "express.Function.use(logger)", "express.Function.use(logger):42", 0);
        SpanBo spanBo = newSpanBo(spanEventBo1);

        SpanAlign.Builder rootAlign = new SpanAlign.Builder(spanBo)
                .disableMeta()
                .setId(0)
                .setGapMillis(0)
                .setDepth(0)
                .setExecutionMillis(12);
        CallTreeNode.Builder root = new CallTreeNode.Builder(rootAlign.build());

        SpanEventAlign.Builder childAlign = new SpanEventAlign.Builder(spanBo, spanEventBo1)
                .setId(0)
                .setGapMillis(0)
                .setDepth(1)
                .setExecutionMillis(1);
        CallTreeNode.Builder child = new CallTreeNode.Builder(childAlign.build());

        root.setChild(child);
        child.setParent(root);

        CallTreeIterator callTreeIterator = new CallTreeIterator(root.build());
        assertThat(callTreeIterator.size()).isEqualTo(2);
        Instant now = Instant.now();
        Predicate<SpanBo> spanMatchFilter = SpanFilters.spanFilter(5997054625569493L, "express-node-sample-id", now.toEpochMilli());

        // annotationKeyRegistryService: DefaultAnnotationKeyRegistryService
        // annotationKeyLocator: AnnotationKeyRegistry TraceMetaDataLoaderService: ServerTraceMetadataLoaderService
        // ServerTraceMetadataLoaderService() -> this.annotationKeyRegistry = traceMetadataLoader.createAnnotationKeyRegistry();
        TraceMetadataLoaderService mockedTypeLoaderService = mock(ServerTraceMetadataLoaderService.class);
        TraceMetadataLoader traceMetadataLoader = new TraceMetadataLoader();
        AnnotationKeyRegistry annotationKeyRegistry = traceMetadataLoader.createAnnotationKeyRegistry();
        when(mockedTypeLoaderService.getAnnotationKeyLocator()).thenReturn(annotationKeyRegistry);
        AnnotationKeyRegistryService mockedAnnotationKeyRegistryService = new DefaultAnnotationKeyRegistryService(mockedTypeLoaderService);

        AnnotationKeyMatcherService mockedAnnotationKeyMatcherService = mock(AnnotationKeyMatcherService.class);

        RecorderFactoryProvider mockedProvider = mock(RecorderFactoryProvider.class);
        ServiceTypeRegistryService mockedRegistry = mock(ServiceTypeRegistryService.class);
        AnnotationRecordFormatter annotationRecordFormatter = mock(AnnotationRecordFormatter.class);
        ApiParserProvider mockedApiParserProvider = new ApiParserProvider();
        when(mockedProvider.getRecordFactory()).thenReturn(new RecordFactory(mockedAnnotationKeyMatcherService, mockedRegistry, mockedAnnotationKeyRegistryService, annotationRecordFormatter, mockedApiParserProvider));

        TransactionInfoService dut = new TransactionInfoServiceImpl(mockedAnnotationKeyMatcherService, Optional.empty(), mockedProvider, mockServiceTypeRegistryService);

        RecordSet actuals = dut.createRecordSet(callTreeIterator, spanMatchFilter);
        assertThat(actuals.getStartTime()).isEqualTo(1670305848569L);
        assertThat(actuals.getEndTime()).isEqualTo(1670305848583L);
        List<com.navercorp.pinpoint.web.trace.callstacks.Record> recordActuals = actuals.getRecordList();
        Record recordActual = recordActuals.get(0);
        assertThat(recordActual).extracting("tab", "id", "parentId", "method", "title", "arguments", "begin", "elapsed", "gap", "agentId", "agentName", "applicationName", "apiServiceType", "destinationId", "hasChild", "hasException", "spanId", "executionMilliseconds", "methodTypeEnum", "isAuthorized", "excludeFromTimeline", "simpleClassName", "fullApiDescription", "lineNumber", "location")
                .contains(0, 1, 0, true, "Node Server Process", "/", 1670305848569L, 14L, 0, "express-node-sample-id", " ", "express-node-sample-name", null, null, true, false, 8174884636707391L, 13L, MethodTypeEnum.WEB_REQUEST, true, true, true, "", "Node Server Process", 0, "");
        recordActual = recordActuals.get(4);
        assertThat(recordActual).extracting("tab", "id", "parentId", "method", "title", "arguments", "begin", "elapsed", "gap", "agentId", "agentName", "applicationName", "apiServiceType", "destinationId", "hasChild", "hasException", "spanId", "executionMilliseconds", "methodTypeEnum", "isAuthorized", "excludeFromTimeline", "simpleClassName", "fullApiDescription", "lineNumber", "location")
                .contains(1, 5, 1, true, "use(logger)", "", 1670305848569L, 1L, 0L, "express-node-sample-id", "", "express-node-sample-name", null, "localhost:3000", false, false, 8174884636707391L, 1L, MethodTypeEnum.DEFAULT, true, true, false, "Function", "express.Function.use(logger)", 42, "/Users/workspace/pinpoint/@pinpoint-naver-apm/pinpoint-agent-node/samples/express/src/app.js");
    }

    private static SpanBo newSpanBo(SpanEventBo spanEventBo1) {
        SpanBo spanBo = new SpanBo();
        spanBo.getSpanOwner().setAgentId("express-node-sample-id");
        spanBo.getSpanOwner().setAgentName("");
        spanBo.getSpanOwner().setApplicationName("express-node-sample-name");
        spanBo.getSpanOwner().setServiceName("express-node-sample-service");
        spanBo.getSpanOwner().setServiceUid(() -> ServiceUid.of(100));
        spanBo.getSpanOwner().setAgentStartTime(1670293953108L);
        spanBo.setTransactionId(new PinpointServerTraceId("express-node-sample-id", 1670293953108L, 30));
        spanBo.setSpanId(8174884636707391L);
        spanBo.setParentSpanId(-1);
//        spanBo.setParentApplicationName(null);
//        spanBo.setParentApplicationServiceType((short) 0);
        spanBo.setTraceTime(SpanVersion.TRACE_V2, 1670305848569L, 14);
        spanBo.setRpc("/");
        spanBo.setServiceType(1400);
        spanBo.setEndPoint("localhost:3000");
        spanBo.setApiId(1);
        spanBo.setFlag((short) 0);
        spanBo.setErrCode(0);
        spanBo.setCollectorAcceptTime(1670305848586L);
        spanBo.setExceptionClass(null);
        spanBo.setApplicationServiceType(1400);
        spanBo.setAcceptorHost(null);
        spanBo.setRemoteAddr("::1");
        spanBo.addAnnotation(AnnotationBo.of(46, 200));
        spanBo.addAnnotation(AnnotationBo.of(13, new ApiMetaDataBo.Builder("express-node-sample-id", 1670293953108L, 1, 0,
                MethodTypeEnum.WEB_REQUEST, "Node Server Process").build()));
        spanBo.addAnnotation(AnnotationBo.of(10015, "Node Server Process"));
        spanBo.addSpanEvent(spanEventBo1);
        spanBo.addSpanEvent(newSpanEventBo((short) 1, 1, 0, 18, 43,
                "express.Function.use(jsonParser)", "express.Function.use(jsonParser):43", -1));
        spanBo.addSpanEvent(newSpanEventBo((short) 2, 1, 0, 19, 44,
                "express.Function.use(urlencodedParser)", "express.Function.use(urlencodedParser):44", -1));
        spanBo.addSpanEvent(newSpanEventBo((short) 3, 1, 0, 20, 45,
                "express.Function.use(cookieParser)", "express.Function.use(cookieParser):45", -1));
        return spanBo;
    }

    private static SpanEventBo newSpanEventBo(short sequence, int startElapsedMillis, int endElapsedMillis,
                                              int apiId, int lineNumber, String apiInfo,
                                              String annotationValue, int nextAsyncId) {
        SpanEventBo spanEventBo = new SpanEventBo();
        spanEventBo.setVersion((byte) 0);
        spanEventBo.setSequence(sequence);
        spanEventBo.setStartElapsed(startElapsedMillis);
        spanEventBo.setEndElapsed(endElapsedMillis);
        spanEventBo.setServiceType(6600);
        spanEventBo.setDestinationId("localhost:3000");
        spanEventBo.setEndPoint("localhost:3000");
        spanEventBo.setApiId(apiId);
        spanEventBo.addAnnotation(AnnotationBo.of(-1, "/"));
        spanEventBo.addAnnotation(AnnotationBo.of(13, new ApiMetaDataBo.Builder("express-node-sample-id", 1670293953108L,
                apiId, lineNumber, MethodTypeEnum.DEFAULT, apiInfo)
                .setLocation("/Users/workspace/pinpoint/@pinpoint-naver-apm/pinpoint-agent-node/samples/express/src/app.js")
                .build()));
        spanEventBo.addAnnotation(AnnotationBo.of(12, annotationValue));
        spanEventBo.setDepth(1);
        spanEventBo.setNextSpanId(-1);
        spanEventBo.setNextAsyncId(nextAsyncId);
        return spanEventBo;
    }
}

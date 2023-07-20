/*
 * Copyright 2019 NAVER Corp.
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

package com.navercorp.pinpoint.web.vo.callstacks;

import com.navercorp.pinpoint.common.profiler.trace.AnnotationKeyRegistry;
import com.navercorp.pinpoint.common.profiler.trace.TraceMetadataLoader;
import com.navercorp.pinpoint.common.profiler.util.TransactionId;
import com.navercorp.pinpoint.common.server.bo.AnnotationBo;
import com.navercorp.pinpoint.common.server.bo.ApiMetaDataBo;
import com.navercorp.pinpoint.common.server.bo.MethodTypeEnum;
import com.navercorp.pinpoint.common.server.bo.SpanBo;
import com.navercorp.pinpoint.common.server.bo.SpanEventBo;
import com.navercorp.pinpoint.common.server.trace.ApiParserProvider;
import com.navercorp.pinpoint.common.server.util.Log4j2CommonLoggerFactory;
import com.navercorp.pinpoint.common.server.util.ServerTraceMetadataLoaderService;
import com.navercorp.pinpoint.loader.service.AnnotationKeyRegistryService;
import com.navercorp.pinpoint.loader.service.DefaultAnnotationKeyRegistryService;
import com.navercorp.pinpoint.loader.service.ServiceTypeRegistryService;
import com.navercorp.pinpoint.loader.service.TraceMetadataLoaderService;
import com.navercorp.pinpoint.web.calltree.span.Align;
import com.navercorp.pinpoint.web.calltree.span.CallTreeIterator;
import com.navercorp.pinpoint.web.calltree.span.CallTreeNode;
import com.navercorp.pinpoint.web.calltree.span.SpanAlign;
import com.navercorp.pinpoint.web.calltree.span.SpanEventAlign;
import com.navercorp.pinpoint.web.calltree.span.SpanFilters;
import com.navercorp.pinpoint.web.dao.TraceDao;
import com.navercorp.pinpoint.web.service.AnnotationKeyMatcherService;
import com.navercorp.pinpoint.web.service.ProxyRequestTypeRegistryService;
import com.navercorp.pinpoint.web.service.RecorderFactoryProvider;
import com.navercorp.pinpoint.web.service.TransactionInfoService;
import com.navercorp.pinpoint.web.service.TransactionInfoServiceImpl;
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
    private ProxyRequestTypeRegistryService mockProxyRequestTypeRegistryService;

    @Mock
    private ApiParserProvider apiParserProvider;

    private RecordFactory newRecordFactory() {
        RecorderFactoryProvider recorderFactoryProvider = new RecorderFactoryProvider(mockServiceTypeRegistryService,
                mockAnnotationKeyMatcherService, mockAnnotationKeyRegistryService, mockProxyRequestTypeRegistryService, apiParserProvider);
        return recorderFactoryProvider.getRecordFactory();
    }

    public void get() {
        // TODO
    }

    @Test
    public void getException_check_argument() {
        final RecordFactory factory = newRecordFactory();

        SpanBo spanBo = new SpanBo();
        spanBo.setTransactionId(new TransactionId("test", 0, 0));
        spanBo.setExceptionInfo(1, null);
        Align align = new SpanAlign(spanBo);


        Record exceptionRecord = factory.getException(0, 0, align);

        Assertions.assertNotNull(exceptionRecord.getArguments());
    }


    @Test
    public void getParameter_check_argument() {

        final RecordFactory factory = newRecordFactory();

        Record exceptionRecord = factory.getParameter(0, 0, "testMethod", null);

        Assertions.assertEquals(exceptionRecord.getArguments(), "null");
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
        SpanEventBo spanEventBo1 = new SpanEventBo.Builder()
                .setVersion(0)
                .setSequence((short) 0)
                .setStartElapsed(0)
                .setEndElapsed(1)
                .setServiceType((short) 6600)
                .setDestinationId("localhost:3000")
                .setEndPoint("localhost:3000")
                .setApiId(17)
                .addAnnotationBo(AnnotationBo.of(-1, "/"))
                .addAnnotationBo(AnnotationBo.of(13, new ApiMetaDataBo.Builder("express-node-sample-id", 1670293953108L, 17, 42,
                        MethodTypeEnum.DEFAULT, "express.Function.use(logger)").setLocation("/Users/workspace/pinpoint/@pinpoint-naver-apm/pinpoint-agent-node/samples/express/src/app.js").build()))
                .addAnnotationBo(AnnotationBo.of(12, "express.Function.use(logger):42"))
                .setDepth(1)
                .setNextSpanId(-1)
                .setNextAsyncId(0)
                .build();
        SpanBo.Builder spanBoBuilder = new SpanBo.Builder(8174884636707391L)
                .setVersion(1)
                .setAgentId("express-node-sample-id")
                .setAgentName("")
                .setApplicationId("express-node-sample-name")
                .setAgentStartTime(1670293953108L)
                .setTransactionId(new TransactionId("express-node-sample-id", 1670293953108L, 30))
                .setParentSpanId(-1)
                .setParentApplicationId(null)
                .setParentApplicationServiceType((short) 0)
                .setStartTime(1670305848569L)
                .setElapsed(14)
                .setRpc("/")
                .setServiceType((short) 1400)
                .setEndPoint("localhost:3000")
                .setApiId(1)
                .setFlag((short) 0)
                .setErrCode(0)
                .setCollectorAcceptTime(1670305848586L)
                .setExceptionClass(null)
                .setApplicationServiceType((short) 1400)
                .setAcceptorHost(null)
                .setRemoteAddr("::1")
                .addAnnotationBo(AnnotationBo.of(46, 200))
                .addAnnotationBo(AnnotationBo.of(13, new ApiMetaDataBo.Builder("express-node-sample-id", 1670293953108L, 1, 0,
                        MethodTypeEnum.WEB_REQUEST, "Node Server Process").build()))
                .addAnnotationBo(AnnotationBo.of(10015, "Node Server Process"))
                .setFlag((short) 0)
                .setErrCode(0)
                .addSpanEventBo(spanEventBo1)
                .addSpanEventBo(new SpanEventBo.Builder()
                        .setSequence((short) 1)
                        .setStartElapsed(1)
                        .setEndElapsed(0)
                        .setServiceType((short) 6600)
                        .setDestinationId("localhost:3000")
                        .setEndPoint("localhost:3000")
                        .setApiId(18)
                        .addAnnotationBo(AnnotationBo.of(-1, "/"))
                        .addAnnotationBo(AnnotationBo.of(13, new ApiMetaDataBo.Builder("express-node-sample-id", 1670293953108L, 18, 43,
                                MethodTypeEnum.DEFAULT, "express.Function.use(jsonParser)").setLocation("/Users/workspace/pinpoint/@pinpoint-naver-apm/pinpoint-agent-node/samples/express/src/app.js").build())
                        )
                        .addAnnotationBo(AnnotationBo.of(12, "express.Function.use(jsonParser):43"))
                        .setDepth(1)
                        .setNextSpanId(-1)
                        .build())
                .addSpanEventBo(new SpanEventBo.Builder()
                        .setSequence((short) 2)
                        .setStartElapsed(1)
                        .setEndElapsed(0)
                        .setServiceType((short) 6600)
                        .setDestinationId("localhost:3000")
                        .setEndPoint("localhost:3000")
                        .setApiId(19)
                        .addAnnotationBo(AnnotationBo.of(-1, "/"))
                        .addAnnotationBo(AnnotationBo.of(13, new ApiMetaDataBo.Builder("express-node-sample-id", 1670293953108L, 19, 44,
                                MethodTypeEnum.DEFAULT, "express.Function.use(urlencodedParser)").setLocation("/Users/workspace/pinpoint/@pinpoint-naver-apm/pinpoint-agent-node/samples/express/src/app.js").build())
                        )
                        .addAnnotationBo(AnnotationBo.of(12, "express.Function.use(urlencodedParser):44"))
                        .setDepth(1)
                        .setNextSpanId(-1)
                        .build())
                .addSpanEventBo(new SpanEventBo.Builder()
                        .setSequence((short) 3)
                        .setStartElapsed(1)
                        .setEndElapsed(0)
                        .setServiceType((short) 6600)
                        .setDestinationId("localhost:3000")
                        .setEndPoint("localhost:3000")
                        .setApiId(20)
                        .addAnnotationBo(AnnotationBo.of(-1, "/"))
                        .addAnnotationBo(AnnotationBo.of(13, new ApiMetaDataBo.Builder("express-node-sample-id", 1670293953108L, 20, 45,
                                MethodTypeEnum.DEFAULT, "express.Function.use(cookieParser)").setLocation("/Users/workspace/pinpoint/@pinpoint-naver-apm/pinpoint-agent-node/samples/express/src/app.js").build())
                         )
                        .addAnnotationBo(AnnotationBo.of(12, "express.Function.use(cookieParser):45"))
                        .setDepth(1)
                        .setNextSpanId(-1)
                        .build())
                .setCollectorAcceptTime(1670305848586L)
                .setApplicationServiceType((short) 1400)
                .setRemoteAddr("::1");

        SpanAlign.Builder rootAlign = new SpanAlign.Builder(spanBoBuilder.build())
                .disableMeta()
                .setId(0)
                .setGap(0)
                .setDepth(0)
                .setExecutionMilliseconds(12);
        CallTreeNode.Builder root = new CallTreeNode.Builder(rootAlign.build());

        SpanEventAlign.Builder childAlign = new SpanEventAlign.Builder(spanBoBuilder.build(), spanEventBo1)
                .setId(0)
                .setGap(0)
                .setDepth(1)
                .setExecutionMilliseconds(1);
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
        // commonLoggerFactory -> Log4j2CommonLoggerFactory
        TraceMetadataLoaderService mockedTypeLoaderService = mock(ServerTraceMetadataLoaderService.class);
        Log4j2CommonLoggerFactory loggerFactory = new Log4j2CommonLoggerFactory();
        TraceMetadataLoader traceMetadataLoader = new TraceMetadataLoader(loggerFactory);
        AnnotationKeyRegistry annotationKeyRegistry = traceMetadataLoader.createAnnotationKeyRegistry();
        when(mockedTypeLoaderService.getAnnotationKeyLocator()).thenReturn(annotationKeyRegistry);
        AnnotationKeyRegistryService mockedAnnotationKeyRegistryService = new DefaultAnnotationKeyRegistryService(mockedTypeLoaderService);

        TraceDao mockedDao = mock(TraceDao.class);
        AnnotationKeyMatcherService mockedAnnotationKeyMatcherService = mock(AnnotationKeyMatcherService.class);

        RecorderFactoryProvider mockedProvider = mock(RecorderFactoryProvider.class);
        ProxyRequestTypeRegistryService mockedProxyRequestTypeRegistryService = mock(ProxyRequestTypeRegistryService.class);
        ServiceTypeRegistryService mockedRegistry = mock(ServiceTypeRegistryService.class);
        ApiParserProvider mockedApiParserProvider = new ApiParserProvider();
        when(mockedProvider.getRecordFactory()).thenReturn(new RecordFactory(mockedAnnotationKeyMatcherService, mockedRegistry, mockedAnnotationKeyRegistryService, mockedProxyRequestTypeRegistryService, mockedApiParserProvider));

        TransactionInfoService dut = new TransactionInfoServiceImpl(mockedDao, mockedAnnotationKeyMatcherService, Optional.empty(), mockedProvider);

        RecordSet actuals = dut.createRecordSet(callTreeIterator, spanMatchFilter);
        assertThat(actuals.getStartTime()).isEqualTo(1670305848569L);
        assertThat(actuals.getEndTime()).isEqualTo(1670305848583L);
        List<Record> recordActuals = actuals.getRecordList();
        Record recordActual = recordActuals.get(0);
        assertThat(recordActual).extracting("tab", "id", "parentId", "method", "title", "arguments", "begin", "elapsed", "gap", "agentId", "agentName", "applicationName", "serviceType", "destinationId", "hasChild", "hasException", "spanId", "executionMilliseconds", "methodTypeEnum", "isAuthorized", "excludeFromTimeline", "focused", "simpleClassName", "fullApiDescription", "lineNumber", "location")
                .contains(0, 1, 0, true, "Node Server Process", "/", 1670305848569L, 14L, 0, "express-node-sample-id", " ", "express-node-sample-name", null, null, true, false, 8174884636707391L, 13L, MethodTypeEnum.WEB_REQUEST, true, true, true, "", "Node Server Process", 0, "");
        recordActual = recordActuals.get(4);
        assertThat(recordActual).extracting("tab", "id", "parentId", "method", "title", "arguments", "begin", "elapsed", "gap", "agentId", "agentName", "applicationName", "serviceType", "destinationId", "hasChild", "hasException", "spanId", "executionMilliseconds", "methodTypeEnum", "isAuthorized", "excludeFromTimeline", "focused", "simpleClassName", "fullApiDescription", "lineNumber", "location")
                .contains(1, 5, 1, true, "use(logger)", "", 1670305848569L, 1L, 0L, "express-node-sample-id", "", "express-node-sample-name", null, "localhost:3000", false, false, 8174884636707391L, 1L, MethodTypeEnum.DEFAULT, true, true, false, "Function", "express.Function.use(logger)", 42, "/Users/workspace/pinpoint/@pinpoint-naver-apm/pinpoint-agent-node/samples/express/src/app.js");
    }
}
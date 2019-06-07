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

package com.navercorp.pinpoint.collector.receiver.grpc.service;

import com.navercorp.pinpoint.collector.receiver.DispatchHandler;
import com.navercorp.pinpoint.grpc.MessageToStringAdapter;
import com.navercorp.pinpoint.grpc.trace.MetadataGrpc;
import com.navercorp.pinpoint.grpc.trace.PApiMetaData;
import com.navercorp.pinpoint.grpc.trace.PResult;
import com.navercorp.pinpoint.grpc.trace.PStringMetaData;
import com.navercorp.pinpoint.io.header.Header;
import com.navercorp.pinpoint.io.header.HeaderEntity;
import com.navercorp.pinpoint.io.header.v2.HeaderV2;
import com.navercorp.pinpoint.io.request.DefaultMessage;
import com.navercorp.pinpoint.io.request.Message;
import com.navercorp.pinpoint.thrift.io.DefaultTBaseLocator;
import io.grpc.stub.StreamObserver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.Objects;

/**
 * @author Woonduk Kang(emeroad)
 */
public class MetadataService extends MetadataGrpc.MetadataImplBase {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private final boolean isDebug = logger.isDebugEnabled();

    private final RequestHandlerAdaptor<PResult> requestHandlerAdaptor;

    public MetadataService(DispatchHandler dispatchHandler) {
        Objects.requireNonNull(dispatchHandler, "dispatchHandler must not be null");
        this.requestHandlerAdaptor = new RequestHandlerAdaptor<>(this.getClass().getName(), dispatchHandler);
    }

    @Override
    public void requestApiMetaData(PApiMetaData apiMetaData, StreamObserver<PResult> responseObserver) {
        if (isDebug) {
            logger.debug("Request PApiMetaData={}", MessageToStringAdapter.getInstance(apiMetaData));
        }

        final Header header = new HeaderV2(Header.SIGNATURE, HeaderV2.VERSION, DefaultTBaseLocator.APIMETADATA);
        final HeaderEntity headerEntity = newEmptyHeaderEntity();
        Message<PApiMetaData> message = new DefaultMessage<PApiMetaData>(header, headerEntity, apiMetaData);

        requestHandlerAdaptor.request(message, responseObserver);
    }


    @Override
    public void requestStringMetaData(PStringMetaData stringMetaData, StreamObserver<PResult> responseObserver) {
        if (isDebug) {
            logger.debug("Request PStringMetaData={}", MessageToStringAdapter.getInstance(stringMetaData));
        }

        final Header header = new HeaderV2(Header.SIGNATURE, HeaderV2.VERSION, DefaultTBaseLocator.STRINGMETADATA);
        final HeaderEntity headerEntity = newEmptyHeaderEntity();
        Message<PStringMetaData> message = new DefaultMessage<PStringMetaData>(header, headerEntity, stringMetaData);

        requestHandlerAdaptor.request(message, responseObserver);
    }

    private HeaderEntity newEmptyHeaderEntity() {
        return new HeaderEntity(Collections.emptyMap());
    }

}

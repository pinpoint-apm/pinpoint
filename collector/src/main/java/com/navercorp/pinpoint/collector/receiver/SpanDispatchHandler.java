/*
 * Copyright 2019 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.navercorp.pinpoint.collector.receiver;

import com.navercorp.pinpoint.collector.handler.SimpleHandler;
import com.navercorp.pinpoint.io.header.Header;
import com.navercorp.pinpoint.io.request.ServerRequest;
import com.navercorp.pinpoint.io.request.ServerResponse;
import com.navercorp.pinpoint.thrift.io.DefaultTBaseLocator;

import java.util.Objects;

/**
 * @author emeroad
 */
public class SpanDispatchHandler implements DispatchHandler {

    private final SimpleHandler spanDataHandler;

    private final SimpleHandler spanChunkHandler;
    

    public SpanDispatchHandler(SimpleHandler spanDataHandler, SimpleHandler spanChunkHandler) {
        this.spanDataHandler = Objects.requireNonNull(spanDataHandler, "spanDataHandler");
        this.spanChunkHandler = Objects.requireNonNull(spanChunkHandler, "spanChunkHandler");
    }

    private SimpleHandler getSimpleHandler(Header header) {
        final short type = header.getType();
        switch (type) {
            case DefaultTBaseLocator.SPAN:
                return spanDataHandler;
            case DefaultTBaseLocator.SPANCHUNK:
                return spanChunkHandler;
        }
        throw new UnsupportedOperationException("unsupported header:" + header);
    }

    @Override
    public void dispatchSendMessage(ServerRequest serverRequest) {
        SimpleHandler simpleHandler = getSimpleHandler(serverRequest.getHeader());
        simpleHandler.handleSimple(serverRequest);
    }


    @Override
    public void dispatchRequestMessage(ServerRequest serverRequest, ServerResponse serverResponse) {

    }
}

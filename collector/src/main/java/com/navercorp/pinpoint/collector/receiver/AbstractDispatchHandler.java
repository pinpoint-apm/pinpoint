/*
 * Copyright 2014 NAVER Corp.
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

package com.navercorp.pinpoint.collector.receiver;

import org.apache.thrift.TBase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.navercorp.pinpoint.collector.handler.Handler;
import com.navercorp.pinpoint.collector.handler.RequestResponseHandler;
import com.navercorp.pinpoint.collector.handler.SimpleHandler;
import com.navercorp.pinpoint.common.server.util.AcceptedTimeService;

/**
 * @author emeroad
 * @author koo.taejin
 */
public abstract class AbstractDispatchHandler implements DispatchHandler {

    protected Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private AcceptedTimeService acceptedTimeService;

    public AbstractDispatchHandler() {
    }


    @Override
    public void dispatchSendMessage(TBase<?, ?> tBase) {

        // mark accepted time
        acceptedTimeService.accept();
        
        // TODO consider to change dispatch table automatically
        SimpleHandler simpleHandler = getSimpleHandler(tBase);
        if (simpleHandler != null) {
            if (logger.isTraceEnabled()) {
                logger.trace("simpleHandler name:{}", simpleHandler.getClass().getName());
            }
            simpleHandler.handleSimple(tBase);
            return;
        }

        Handler handler = getHandler(tBase);
        if (handler != null) {
            if (logger.isTraceEnabled()) {
                logger.trace("handler name:{}", handler.getClass().getName());
            }
            handler.handle(tBase);
            return;
        }

        throw new UnsupportedOperationException("Handler not found. Unknown type of data received. tBase=" + tBase);
    }
    
    public TBase dispatchRequestMessage(TBase<?,?> tBase) {
        // mark accepted time
        acceptedTimeService.accept();

        RequestResponseHandler requestResponseHandler = getRequestResponseHandler(tBase);
        if (requestResponseHandler != null) {
            if (logger.isTraceEnabled()) {
                logger.trace("requestResponseHandler name:{}", requestResponseHandler.getClass().getName());
            }
            return requestResponseHandler.handleRequest(tBase);
        }

        throw new UnsupportedOperationException("Handler not found. Unknown type of data received. tBase=" + tBase);
    }

    Handler getHandler(TBase<?, ?> tBase) {
        return null;
    }


    RequestResponseHandler getRequestResponseHandler(TBase<?, ?> tBase) {
        return null;
    }


    SimpleHandler getSimpleHandler(TBase<?, ?> tBase) {
        return null;
    }
}

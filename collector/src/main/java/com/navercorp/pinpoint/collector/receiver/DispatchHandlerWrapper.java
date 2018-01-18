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

import com.navercorp.pinpoint.collector.manage.HandlerManager;
import com.navercorp.pinpoint.thrift.dto.TResult;

import java.util.Objects;

/**
 * @author Taejin Koo
 */
public class DispatchHandlerWrapper implements DispatchHandler {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    
    private final DispatchHandler delegate;

    private final HandlerManager handlerManager;

    public DispatchHandlerWrapper(DispatchHandler delegate, HandlerManager handlerManager) {
        this.delegate = Objects.requireNonNull(delegate, "delegate must not be null");
        this.handlerManager = Objects.requireNonNull(handlerManager, "handlerManager must not be null");
    }

    @Override
    public void dispatchSendMessage(TBase<?, ?> tBase) {
        if (checkAvailable()) {
            this.delegate.dispatchSendMessage(tBase);
            return;
        }

        logger.debug("Handler is disabled. Skipping send message {}.", tBase);
    }

    @Override
    public TBase dispatchRequestMessage(TBase<?, ?> tBase) {
        if (checkAvailable()) {
            return this.delegate.dispatchRequestMessage(tBase);
        }

        logger.debug("Handler is disabled. Skipping request message {}.", tBase);
        
        TResult result = new TResult(false);
        result.setMessage("Handler is disabled. Skipping request message.");
        return result;
    }
    
    private boolean checkAvailable() {
        if (handlerManager.isEnable()) {
            return true;
        }
        
        return false;
    }

}

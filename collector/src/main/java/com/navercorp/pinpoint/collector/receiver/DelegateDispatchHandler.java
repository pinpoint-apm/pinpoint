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

package com.navercorp.pinpoint.collector.receiver;

import com.navercorp.pinpoint.collector.manage.HandlerManager;
import com.navercorp.pinpoint.io.request.ServerRequest;
import com.navercorp.pinpoint.io.request.ServerResponse;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Objects;

/**
 * @author Woonduk Kang(emeroad)
 */
public class DelegateDispatchHandler<REQ, RES> implements DispatchHandler<REQ, RES> {
    private final Logger logger = LogManager.getLogger(this.getClass());

    private final DispatchHandler<REQ, RES> delegate;

    private final HandlerManager handlerManager;

    public DelegateDispatchHandler(DispatchHandler<REQ, RES> delegate, HandlerManager handlerManager) {
        this.delegate = Objects.requireNonNull(delegate, "delegate");
        this.handlerManager = Objects.requireNonNull(handlerManager, "handlerManager");
    }


    @Override
    public void dispatchSendMessage(ServerRequest<REQ> serverRequest) {

        if (!checkAvailable()) {
            logger.debug("Handler is disabled. Skipping send message {}.", serverRequest);
            return;
        }

        this.delegate.dispatchSendMessage(serverRequest);
    }


    @Override
    public void dispatchRequestMessage(ServerRequest<REQ> serverRequest, ServerResponse<RES> serverResponse) {

        if (!checkAvailable()) {
            logger.debug("Handler is disabled. Skipping request message {}.", serverRequest);
            serverResponse.finish();
            return;
        }

        delegate.dispatchRequestMessage(serverRequest, serverResponse);

    }


    private boolean checkAvailable() {
        if (handlerManager.isEnable()) {
            return true;
        }

        return false;
    }
}

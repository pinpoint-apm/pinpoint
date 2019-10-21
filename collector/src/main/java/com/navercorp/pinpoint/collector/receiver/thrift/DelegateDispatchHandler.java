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

package com.navercorp.pinpoint.collector.receiver.thrift;

import com.navercorp.pinpoint.collector.manage.HandlerManager;
import com.navercorp.pinpoint.collector.receiver.DispatchHandler;
import com.navercorp.pinpoint.common.server.util.AcceptedTimeService;
import com.navercorp.pinpoint.io.request.ServerRequest;
import com.navercorp.pinpoint.io.request.ServerResponse;
import com.navercorp.pinpoint.thrift.dto.TResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;

/**
 * @author Woonduk Kang(emeroad)
 */
public class DelegateDispatchHandler implements DispatchHandler {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final AcceptedTimeService acceptedTimeService;
    private final DispatchHandler delegate;

    private final HandlerManager handlerManager;

    public DelegateDispatchHandler(AcceptedTimeService acceptedTimeService, DispatchHandler delegate, HandlerManager handlerManager) {
        this.acceptedTimeService = Objects.requireNonNull(acceptedTimeService, "acceptedTimeService");
        this.delegate = Objects.requireNonNull(delegate, "delegate");
        this.handlerManager = Objects.requireNonNull(handlerManager, "handlerManager");
    }


    @Override
    public void dispatchSendMessage(ServerRequest serverRequest) {
        acceptedTimeService.accept();

        if (!checkAvailable()) {
            logger.debug("Handler is disabled. Skipping send message {}.", serverRequest);
            return;
        }

        this.delegate.dispatchSendMessage(serverRequest);
    }


    @Override
    public void dispatchRequestMessage(ServerRequest serverRequest, ServerResponse serverResponse) {
        acceptedTimeService.accept();

        if (!checkAvailable()) {
            logger.debug("Handler is disabled. Skipping request message {}.", serverRequest);
            TResult result = new TResult(false);
            result.setMessage("Handler is disabled. Skipping request message.");
            serverResponse.write(result);
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

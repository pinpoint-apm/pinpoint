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
import com.navercorp.pinpoint.io.request.ServerHeader;
import com.navercorp.pinpoint.io.request.ServerRequest;
import com.navercorp.pinpoint.io.request.ServerResponse;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Objects;

/**
 * @author Woonduk Kang(emeroad)
 */
public class SimpleRequestHandlerAdaptor<REQ, RES> {
    private final Logger logger;

    private final DispatchHandler<REQ, RES> dispatchHandler;

    public SimpleRequestHandlerAdaptor(String name, DispatchHandler<REQ, RES> dispatchHandler) {
        Objects.requireNonNull(name, "name");
        this.logger = LogManager.getLogger(name);
        this.dispatchHandler = Objects.requireNonNull(dispatchHandler, "dispatchHandler");
    }

    @SuppressWarnings("unchecked")
    public void dispatch(ServerRequest<? extends REQ> request, ServerResponse<? extends RES> response) {
        final ServerHeader header = request.getHeader();
        try {
            this.dispatchHandler.dispatchRequestMessage((ServerRequest<REQ>) request, (ServerResponse<RES>) response);
        } catch (Exception e) {
            logger.warn("Failed to request. header={}", header, e);
            response.onError(e);
        }
    }

}

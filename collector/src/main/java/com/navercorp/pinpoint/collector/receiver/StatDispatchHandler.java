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

import com.navercorp.pinpoint.collector.handler.SimpleDualHandler;
import com.navercorp.pinpoint.collector.handler.SimpleHandler;
import com.navercorp.pinpoint.io.header.Header;
import com.navercorp.pinpoint.io.request.ServerRequest;
import com.navercorp.pinpoint.io.request.ServerResponse;
import com.navercorp.pinpoint.io.util.MessageType;
import org.apache.commons.lang3.ArrayUtils;

import java.util.Objects;

/**
 * @author emeroad
 * @author hyungil.jeong
 */
public class StatDispatchHandler<REQ, RES> implements DispatchHandler<REQ, RES> {

    private static final int[] DISPATCH_TABLE = new int[]{
            MessageType.AGENT_STAT.getCode(),
            MessageType.AGENT_STAT_BATCH.getCode(),
            MessageType.AGENT_URI_STAT.getCode()
    };

    private final SimpleHandler<REQ> agentStatHandler;

    private final SimpleHandler<REQ> agentEventHandler;

    public StatDispatchHandler(SimpleHandler<REQ> agentStatHandler, SimpleHandler<REQ> agentEventHandler) {
        this.agentStatHandler = Objects.requireNonNull(agentStatHandler, "agentStatHandler");
        this.agentEventHandler = Objects.requireNonNull(agentEventHandler, "agentEventHandler");
    }

    private SimpleHandler<REQ> getSimpleHandler(Header header) {
        final int type = header.getType();
        if (ArrayUtils.contains(DISPATCH_TABLE, type)) {
            return new SimpleDualHandler<>(agentStatHandler, agentEventHandler);
        }

        throw new UnsupportedOperationException("unsupported header:" + header);
    }

    @Override
    public void dispatchSendMessage(ServerRequest<REQ> serverRequest) {
        SimpleHandler<REQ> simpleHandler = getSimpleHandler(serverRequest.getHeader());
        simpleHandler.handleSimple(serverRequest);
    }

    @Override
    public void dispatchRequestMessage(ServerRequest<REQ> serverRequest, ServerResponse<RES> serverResponse) {

    }

}

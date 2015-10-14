/*
 *
 *  * Copyright 2014 NAVER Corp.
 *  *
 *  * Licensed under the Apache License, Version 2.0 (the "License");
 *  * you may not use this file except in compliance with the License.
 *  * You may obtain a copy of the License at
 *  *
 *  *     http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  * Unless required by applicable law or agreed to in writing, software
 *  * distributed under the License is distributed on an "AS IS" BASIS,
 *  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  * See the License for the specific language governing permissions and
 *  * limitations under the License.
 *
 *
 */

package com.navercorp.pinpoint.web.websocket;

import com.navercorp.pinpoint.rpc.packet.stream.StreamClosePacket;
import com.navercorp.pinpoint.rpc.packet.stream.StreamCode;
import com.navercorp.pinpoint.rpc.packet.stream.StreamCreateFailPacket;
import com.navercorp.pinpoint.rpc.packet.stream.StreamResponsePacket;
import com.navercorp.pinpoint.rpc.stream.*;
import com.navercorp.pinpoint.thrift.dto.command.TCmdActiveThreadCount;
import com.navercorp.pinpoint.thrift.dto.command.TCmdActiveThreadCountRes;
import com.navercorp.pinpoint.thrift.dto.command.TCommandTransferResponse;
import com.navercorp.pinpoint.thrift.dto.command.TRouteResult;
import com.navercorp.pinpoint.web.service.AgentService;
import com.navercorp.pinpoint.web.vo.AgentActiveThreadCount;
import com.navercorp.pinpoint.web.vo.AgentInfo;
import org.apache.thrift.TBase;
import org.apache.thrift.TException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @Author Taejin Koo
 */
public class ActiveThreadCountStreamListener implements ClientStreamChannelMessageListener, StreamChannelStateChangeEventHandler<ClientStreamChannel> {

    private static final ClientStreamChannelMessageListener LOGGING = LoggingStreamChannelMessageListener.CLIENT_LISTENER;

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final AgentService agentService;
    private final AgentInfo agentInfo;
    private final WebSocketResponseAggregator responseAggregator;
    private final AgentActiveThreadCount defaultFailedResponse;

    private StreamChannel streamchannel;

    public ActiveThreadCountStreamListener(AgentService agentService, AgentInfo agentInfo, WebSocketResponseAggregator webSocketResponseAggregator) {
        this.agentService = agentService;
        this.agentInfo = agentInfo;
        this.responseAggregator = webSocketResponseAggregator;

        this.defaultFailedResponse = new AgentActiveThreadCount(agentInfo.getAgentId());
    }

    public void start() {
        try {
            ClientStreamChannelContext clientStreamChannelContext = agentService.openStream(agentInfo, new TCmdActiveThreadCount(), this);

            if (clientStreamChannelContext.getCreateFailPacket() == null) {
                streamchannel = clientStreamChannelContext.getStreamChannel();
                streamchannel.addStateChangeEventHandler(this);
                defaultFailedResponse.setFail(TRouteResult.TIMEOUT.name());
            } else {
                StreamCreateFailPacket createFailPacket = clientStreamChannelContext.getCreateFailPacket();
                defaultFailedResponse.setFail(createFailPacket.getCode().name());
            }
        } catch (TException exception) {
            defaultFailedResponse.setFail(TRouteResult.NOT_SUPPORTED_REQUEST.name());
        } finally {
            this.responseAggregator.registerStreamMessageListener(agentInfo.getAgentId(), this);
        }
    }

    public void stop() {
        try {
            if (streamchannel != null) {
                streamchannel.close();
            }
            defaultFailedResponse.setFail(StreamCode.STATE_CLOSED.name());
        } finally {
            this.responseAggregator.unregisterStreamMessageListener(agentInfo.getAgentId());
        }
    }

    @Override
    public void handleStreamData(ClientStreamChannelContext streamChannelContext, StreamResponsePacket packet) {
        LOGGING.handleStreamData(streamChannelContext, packet);

        TBase response = agentService.deserializeResponse(packet.getPayload(), null);
        AgentActiveThreadCount activeThreadCount = getAgentActiveThreadCount(response);
        responseAggregator.response(activeThreadCount);
    }

    private AgentActiveThreadCount getAgentActiveThreadCount(TBase routeResponse) {
        AgentActiveThreadCount agentActiveThreadCount = new AgentActiveThreadCount(agentInfo.getAgentId());

        if (routeResponse != null && (routeResponse instanceof TCommandTransferResponse)) {
            byte[] payload = ((TCommandTransferResponse) routeResponse).getPayload();
            TBase<?, ?> activeThreadCountResponse = agentService.deserializeResponse(payload, null);

            if (activeThreadCountResponse != null && (activeThreadCountResponse instanceof TCmdActiveThreadCountRes)) {
                agentActiveThreadCount.setResult((TCmdActiveThreadCountRes) activeThreadCountResponse);
            } else {
                agentActiveThreadCount.setFail("ROUTE_ERROR:" + TRouteResult.NOT_SUPPORTED_RESPONSE.name());
            }
        } else {
            agentActiveThreadCount.setFail("ROUTE_ERROR:" + TRouteResult.BAD_RESPONSE.name());
        }

        return agentActiveThreadCount;
    }

    @Override
    public void handleStreamClose(ClientStreamChannelContext streamChannelContext, StreamClosePacket packet) {
        LOGGING.handleStreamClose(streamChannelContext, packet);

        defaultFailedResponse.setFail(StreamCode.STATE_CLOSED.name());
    }

    @Override
    public void eventPerformed(ClientStreamChannel streamChannel, StreamChannelStateCode updatedStateCode) throws Exception {
        logger.info("eventPerformed streamChannel:{}, stateCode:{}", streamChannel, updatedStateCode);

        switch (updatedStateCode) {
            case CLOSED:
            case ILLEGAL_STATE:
                defaultFailedResponse.setFail(StreamCode.STATE_CLOSED.name());
                break;
        }
    }

    @Override
    public void exceptionCaught(ClientStreamChannel streamChannel, StreamChannelStateCode updatedStateCode, Throwable e) {
        logger.warn("exceptionCaught message:{}, streamChannel:{}, stateCode:{}", e.getMessage(), streamChannel, updatedStateCode, e);
    }

    public AgentInfo getAgentInfo() {
        return agentInfo;
    }

    public AgentActiveThreadCount getDefaultFailedResponse() {
        return defaultFailedResponse;
    }

}

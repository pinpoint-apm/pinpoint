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
public class ActiveThreadCountWorker implements PinpointWebSocketHandlerWorker {

    private static final ClientStreamChannelMessageListener LOGGING = LoggingStreamChannelMessageListener.CLIENT_LISTENER;

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private static final TCmdActiveThreadCount COMMAND_INSTANCE = new TCmdActiveThreadCount();

    private final Object lock = new Object();
    private boolean started = false;
    private boolean stopped = false;

    private final AgentService agentService;
    private final AgentInfo agentInfo;
    private final PinpointWebSocketResponseAggregator responseAggregator;
    private final StreamConnectionManager streamConnectionManager;

    private final AgentActiveThreadCount defaultFailedResponse;

    private final MessageListener messageListener;
    private final StateChangeListener stateChangeListener;

    private StreamChannel streamChannel;

    public ActiveThreadCountWorker(AgentService agentService, AgentInfo agentInfo, PinpointWebSocketResponseAggregator webSocketResponseAggregator, StreamConnectionManager streamConnectionManager) {
        this.agentService = agentService;
        this.agentInfo = agentInfo;

        this.responseAggregator = webSocketResponseAggregator;
        this.streamConnectionManager = streamConnectionManager;

        this.defaultFailedResponse = new AgentActiveThreadCount(agentInfo.getAgentId());

        this.messageListener = new MessageListener();
        this.stateChangeListener = new StateChangeListener();
    }


    @Override
    public void active() {
        synchronized (lock) {
            if (started) {
                return;
            }
            started = true;

            logger.info("ActiveThreadCountWorker start. applicationName:{}, agentId:{}", agentInfo.getApplicationName(), agentInfo.getAgentId());

            try {
                ClientStreamChannelContext clientStreamChannelContext = agentService.openStream(agentInfo, COMMAND_INSTANCE, messageListener, stateChangeListener);
                if (clientStreamChannelContext == null) {
                    defaultFailedResponse.setFail(StreamCode.CONNECTION_NOT_FOUND.name());
                    streamConnectionManager.addReconnectJob(agentInfo, COMMAND_INSTANCE, messageListener, stateChangeListener);
                } else {
                    if (clientStreamChannelContext.getCreateFailPacket() == null) {
                        streamChannel = clientStreamChannelContext.getStreamChannel();
                    } else {
                        StreamCreateFailPacket createFailPacket = clientStreamChannelContext.getCreateFailPacket();
                        defaultFailedResponse.setFail(createFailPacket.getCode().name());
                    }
                }
            } catch (TException exception) {
                defaultFailedResponse.setFail(TRouteResult.NOT_SUPPORTED_REQUEST.name());
            }
        }
    }

    @Override
    public boolean reactive() {
        return false;
    }

    @Override
    public void inactive() {
        synchronized (lock) {
            if (!started && stopped) {
                return;
            }
            stopped = true;

            logger.info("ActiveThreadCountWorker stop. agentId:{}, streamChannel:{}", agentInfo.getAgentId(), streamChannel);

            try {
                streamConnectionManager.removeReconnectJob(agentInfo);
                closeStreamChannel();
            } catch (Exception e) {
            }
        }
    }

    private void closeStreamChannel() {
        if (streamChannel != null) {
            streamChannel.close();
        }
        defaultFailedResponse.setFail(StreamCode.STATE_CLOSED.name());
    }

    public AgentInfo getAgentInfo() {
        return agentInfo;
    }

    public AgentActiveThreadCount getDefaultFailedResponse() {
        return defaultFailedResponse;
    }

    private void setStreamChannel(ClientStreamChannel streamChannel) {
        this.streamChannel = streamChannel;
    }

    private class MessageListener implements ClientStreamChannelMessageListener {

        @Override
        public void handleStreamData(ClientStreamChannelContext streamChannelContext, StreamResponsePacket packet) {
            LOGGING.handleStreamData(streamChannelContext, packet);

            TBase response = agentService.deserializeResponse(packet.getPayload(), null);
            AgentActiveThreadCount activeThreadCount = getAgentActiveThreadCount(response);
            responseAggregator.response(activeThreadCount);
        }

        @Override
        public void handleStreamClose(ClientStreamChannelContext streamChannelContext, StreamClosePacket packet) {
            LOGGING.handleStreamClose(streamChannelContext, packet);

            defaultFailedResponse.setFail(StreamCode.STATE_CLOSED.name());
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

    }

    private class StateChangeListener implements StreamChannelStateChangeEventHandler<ClientStreamChannel> {

        @Override
        public void eventPerformed(ClientStreamChannel streamChannel, StreamChannelStateCode updatedStateCode) throws Exception {
            logger.info("eventPerformed streamChannel:{}, stateCode:{}", streamChannel, updatedStateCode);

            switch (updatedStateCode) {
                case CONNECTED:
                    setStreamChannel(streamChannel);
                    defaultFailedResponse.setFail(TRouteResult.TIMEOUT.name());
                    break;
                case CLOSED:
                case ILLEGAL_STATE:
                    if (!stopped) {
                        streamConnectionManager.addReconnectJob(agentInfo, COMMAND_INSTANCE, messageListener, stateChangeListener);
                    }
                    defaultFailedResponse.setFail(StreamCode.STATE_CLOSED.name());
                    break;
            }
        }

        @Override
        public void exceptionCaught(ClientStreamChannel streamChannel, StreamChannelStateCode updatedStateCode, Throwable e) {
            logger.warn("exceptionCaught message:{}, streamChannel:{}, stateCode:{}", e.getMessage(), streamChannel, updatedStateCode, e);
        }

    }

}

/*
 * Copyright 2017 NAVER Corp.
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

package com.navercorp.pinpoint.web.websocket;

import com.navercorp.pinpoint.rpc.packet.stream.StreamClosePacket;
import com.navercorp.pinpoint.rpc.packet.stream.StreamCode;
import com.navercorp.pinpoint.rpc.packet.stream.StreamCreateFailPacket;
import com.navercorp.pinpoint.rpc.packet.stream.StreamResponsePacket;
import com.navercorp.pinpoint.rpc.stream.ClientStreamChannel;
import com.navercorp.pinpoint.rpc.stream.ClientStreamChannelContext;
import com.navercorp.pinpoint.rpc.stream.ClientStreamChannelMessageListener;
import com.navercorp.pinpoint.rpc.stream.LoggingStreamChannelMessageListener;
import com.navercorp.pinpoint.rpc.stream.StreamChannel;
import com.navercorp.pinpoint.rpc.stream.StreamChannelStateChangeEventHandler;
import com.navercorp.pinpoint.rpc.stream.StreamChannelStateCode;
import com.navercorp.pinpoint.thrift.dto.command.TCmdActiveThreadCount;
import com.navercorp.pinpoint.thrift.dto.command.TCommandTransferResponse;
import com.navercorp.pinpoint.thrift.dto.command.TRouteResult;
import com.navercorp.pinpoint.web.service.AgentService;
import com.navercorp.pinpoint.web.vo.AgentActiveThreadCount;
import com.navercorp.pinpoint.web.vo.AgentActiveThreadCountFactory;
import com.navercorp.pinpoint.web.vo.AgentInfo;
import org.apache.thrift.TBase;
import org.apache.thrift.TException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Taejin Koo
 */
public class ActiveThreadCountWorker implements PinpointWebSocketHandlerWorker {

    private static final ClientStreamChannelMessageListener LOGGING = LoggingStreamChannelMessageListener.CLIENT_LISTENER;
    private static final TCmdActiveThreadCount COMMAND_INSTANCE = new TCmdActiveThreadCount();

    private static final ActiveThreadCountErrorType INTERNAL_ERROR = ActiveThreadCountErrorType.PINPOINT_INTERNAL_ERROR;

    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private final Object lock = new Object();
    private final AgentService agentService;

    private final String applicationName;
    private final String agentId;

    private final PinpointWebSocketResponseAggregator responseAggregator;
    private final WorkerActiveManager workerActiveManager;

    private final AgentActiveThreadCountFactory failResponseFactory;
    private volatile AgentActiveThreadCount defaultFailResponse;

    private final MessageListener messageListener;
    private final StateChangeListener stateChangeListener;

    private volatile boolean started = false;
    private volatile boolean active = false;
    private volatile boolean stopped = false;

    private StreamChannel streamChannel;

    public ActiveThreadCountWorker(AgentService agentService, AgentInfo agentInfo, PinpointWebSocketResponseAggregator webSocketResponseAggregator, WorkerActiveManager workerActiveManager) {
        this(agentService, agentInfo.getApplicationName(), agentInfo.getAgentId(), webSocketResponseAggregator, workerActiveManager);
    }

    public ActiveThreadCountWorker(AgentService agentService, String applicationName, String agentId, PinpointWebSocketResponseAggregator webSocketResponseAggregator, WorkerActiveManager workerActiveManager) {
        this.agentService = agentService;

        this.applicationName = applicationName;
        this.agentId = agentId;

        this.responseAggregator = webSocketResponseAggregator;
        this.workerActiveManager = workerActiveManager;

        AgentActiveThreadCountFactory failResponseFactory = new AgentActiveThreadCountFactory();
        failResponseFactory.setAgentId(agentId);

        this.failResponseFactory = failResponseFactory;

        this.defaultFailResponse = failResponseFactory.createFail(INTERNAL_ERROR.getMessage());

        this.messageListener = new MessageListener();
        this.stateChangeListener = new StateChangeListener();
    }

    @Override
    public void start(AgentInfo agentInfo) {
        if (!applicationName.equals(agentInfo.getApplicationName())) {
            return;
        }

        if (!agentId.equals(agentInfo.getAgentId())) {
            return;
        }

        synchronized (lock) {
            if (!started) {
                started = true;

                logger.info("ActiveThreadCountWorker start. applicationName:{}, agentId:{}", applicationName, agentId);
                this.active = active0(agentInfo);
            }
        }
    }

    @Override
    public boolean reactive(AgentInfo agentInfo) {
        synchronized (lock) {
            if (isTurnOn()) {
                if (active) {
                    return true;
                }

                logger.info("ActiveThreadCountWorker reactive. applicationName:{}, agentId:{}", applicationName, agentId);
                active = active0(agentInfo);
                return active;
            }
        }

        return false;
    }

    @Override
    public void stop() {
        synchronized (lock) {
            if (isTurnOn()) {
                stopped = true;

                logger.info("ActiveThreadCountWorker stop. applicationName:{}, agentId:{}, streamChannel:{}", applicationName, agentId, streamChannel);

                try {
                    closeStreamChannel();
                } catch (Exception ignored) {
                }
                return;
            }
        }
    }

    private boolean active0(AgentInfo agentInfo) {
        synchronized (lock) {
            boolean active = false;
            try {
                ClientStreamChannelContext clientStreamChannelContext = agentService.openStream(agentInfo, COMMAND_INSTANCE, messageListener, stateChangeListener);
                if (clientStreamChannelContext == null) {
                    setDefaultErrorMessage(StreamCode.CONNECTION_NOT_FOUND.name());
                    workerActiveManager.addReactiveWorker(agentInfo);
                } else {
                    if (clientStreamChannelContext.getCreateFailPacket() == null) {
                        streamChannel = clientStreamChannelContext.getStreamChannel();
                        setDefaultErrorMessage(TRouteResult.TIMEOUT.name());
                        active = true;
                    } else {
                        StreamCreateFailPacket createFailPacket = clientStreamChannelContext.getCreateFailPacket();
                        setDefaultErrorMessage(createFailPacket.getCode().name());
                    }
                }
            } catch (TException exception) {
                setDefaultErrorMessage(TRouteResult.NOT_SUPPORTED_REQUEST.name());
            }

            return active;
        }
    }

    private boolean isTurnOn() {
        if (started && !stopped) {
            return true;
        } else {
            return false;
        }
    }

    private void closeStreamChannel() {
        if (streamChannel != null) {
            streamChannel.close();
        }
        setDefaultErrorMessage(StreamCode.STATE_CLOSED.name());
    }

    private void setDefaultErrorMessage(String message) {
        ActiveThreadCountErrorType errorType = ActiveThreadCountErrorType.getType(message);

        AgentActiveThreadCount failResponse = failResponseFactory.createFail(errorType.getCode(), errorType.getMessage());
        defaultFailResponse = failResponse;
    }

    public String getAgentId() {
        return agentId;
    }

    public AgentActiveThreadCount getDefaultFailResponse() {
        return defaultFailResponse;
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
            setDefaultErrorMessage(StreamCode.STATE_CLOSED.name());
        }

        private AgentActiveThreadCount getAgentActiveThreadCount(TBase routeResponse) {
            if (routeResponse instanceof TCommandTransferResponse) {
                byte[] payload = ((TCommandTransferResponse) routeResponse).getPayload();
                TBase<?, ?> activeThreadCountResponse = agentService.deserializeResponse(payload, null);

                AgentActiveThreadCountFactory factory = new AgentActiveThreadCountFactory();
                factory.setAgentId(agentId);
                return factory.create(activeThreadCountResponse);
            } else {
                logger.warn("getAgentActiveThreadCount failed. applicationName:{}, agentId:{}", applicationName, agentId);

                AgentActiveThreadCountFactory factory = new AgentActiveThreadCountFactory();
                factory.setAgentId(agentId);
                return factory.createFail(INTERNAL_ERROR.getMessage());
            }
        }

    }

    private class StateChangeListener implements StreamChannelStateChangeEventHandler<ClientStreamChannel> {

        @Override
        public void eventPerformed(ClientStreamChannel streamChannel, StreamChannelStateCode updatedStateCode) throws Exception {
            logger.info("eventPerformed streamChannel:{}, stateCode:{}", streamChannel, updatedStateCode);

            switch (updatedStateCode) {
                case CLOSED:
                case ILLEGAL_STATE:
                    if (isTurnOn()) {
                        active = false;
                        workerActiveManager.addReactiveWorker(agentId);
                        setDefaultErrorMessage(StreamCode.STATE_CLOSED.name());
                    }
                    break;
            }
        }

        @Override
        public void exceptionCaught(ClientStreamChannel streamChannel, StreamChannelStateCode updatedStateCode, Throwable e) {
            logger.warn("exceptionCaught message:{}, streamChannel:{}, stateCode:{}", e.getMessage(), streamChannel, updatedStateCode, e);
        }

    }

}

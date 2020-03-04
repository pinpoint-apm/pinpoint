/*
 * Copyright 2018 NAVER Corp.
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

package com.navercorp.pinpoint.web.websocket;

import com.navercorp.pinpoint.rpc.packet.stream.StreamClosePacket;
import com.navercorp.pinpoint.rpc.packet.stream.StreamCode;
import com.navercorp.pinpoint.rpc.packet.stream.StreamResponsePacket;
import com.navercorp.pinpoint.rpc.stream.ClientStreamChannel;
import com.navercorp.pinpoint.rpc.stream.ClientStreamChannelEventHandler;
import com.navercorp.pinpoint.rpc.stream.StreamChannel;
import com.navercorp.pinpoint.rpc.stream.StreamChannelStateCode;
import com.navercorp.pinpoint.rpc.stream.StreamException;
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

import java.util.Objects;

/**
 * @author Taejin Koo
 */
public class ActiveThreadCountWorker implements PinpointWebSocketHandlerWorker {

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

    private final EventHandler eventHandler = new EventHandler();

    private volatile boolean started = false;
    private volatile boolean active = false;
    private volatile boolean stopped = false;

    private StreamChannel streamChannel;

    public ActiveThreadCountWorker(AgentService agentService, AgentInfo agentInfo, PinpointWebSocketResponseAggregator webSocketResponseAggregator, WorkerActiveManager workerActiveManager) {
        this(agentService, agentInfo.getApplicationName(), agentInfo.getAgentId(), webSocketResponseAggregator, workerActiveManager);
    }

    public ActiveThreadCountWorker(AgentService agentService, String applicationName, String agentId, PinpointWebSocketResponseAggregator webSocketResponseAggregator, WorkerActiveManager workerActiveManager) {
        this.agentService = Objects.requireNonNull(agentService, "agentService");
        this.applicationName = Objects.requireNonNull(applicationName, "applicationName");
        this.agentId = Objects.requireNonNull(agentId, "agentId");

        this.responseAggregator = Objects.requireNonNull(webSocketResponseAggregator, "responseAggregator");
        this.workerActiveManager = Objects.requireNonNull(workerActiveManager, "workerActiveManager");

        AgentActiveThreadCountFactory failResponseFactory = new AgentActiveThreadCountFactory();
        failResponseFactory.setAgentId(agentId);

        this.failResponseFactory = failResponseFactory;

        this.defaultFailResponse = failResponseFactory.createFail(INTERNAL_ERROR.getMessage());
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
            try {
                streamChannel = agentService.openStream(agentInfo, COMMAND_INSTANCE, eventHandler);
                setDefaultErrorMessage(TRouteResult.TIMEOUT.name());
                return true;
            } catch (StreamException streamException) {
                StreamCode streamCode = streamException.getStreamCode();
                if (streamCode == StreamCode.CONNECTION_NOT_FOUND) {
                    workerActiveManager.addReactiveWorker(agentInfo);
                }
                setDefaultErrorMessage(streamCode.name());
            } catch (TException exception) {
                setDefaultErrorMessage(TRouteResult.NOT_SUPPORTED_REQUEST.name());
            }
            return false;
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


    private class EventHandler extends ClientStreamChannelEventHandler {

        @Override
        public void handleStreamResponsePacket(ClientStreamChannel streamChannel, StreamResponsePacket packet) {
            if (logger.isDebugEnabled()) {
                logger.debug("handleStreamResponsePacket() streamChannel:{}, packet:{}", streamChannel, packet);
            }

            TBase response = agentService.deserializeResponse(packet.getPayload(), null);
            AgentActiveThreadCount activeThreadCount = getAgentActiveThreadCount(response);
            responseAggregator.response(activeThreadCount);
        }

        @Override
        public void handleStreamClosePacket(ClientStreamChannel streamChannel, StreamClosePacket packet) {
            if (logger.isDebugEnabled()) {
                logger.debug("handleStreamClosePacket() streamChannel:{}, packet:{}", streamChannel, packet);
            }

            setDefaultErrorMessage(StreamCode.STATE_CLOSED.name());
        }

        @Override
        public void stateUpdated(ClientStreamChannel streamChannel, StreamChannelStateCode updatedStateCode) {
            if (logger.isDebugEnabled()) {
                logger.debug("stateUpdated() streamChannel:{}, stateCode:{}", streamChannel, updatedStateCode);
            }

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

}

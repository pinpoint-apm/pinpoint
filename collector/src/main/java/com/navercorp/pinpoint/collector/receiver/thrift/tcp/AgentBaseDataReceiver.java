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

package com.navercorp.pinpoint.collector.receiver.thrift.tcp;

import com.navercorp.pinpoint.collector.cluster.ClusterPointStateChangedEventHandler;
import com.navercorp.pinpoint.collector.cluster.ProfilerClusterManager;
import com.navercorp.pinpoint.collector.cluster.zookeeper.ZookeeperClusterService;
import com.navercorp.pinpoint.collector.config.AgentBaseDataReceiverConfiguration;
import com.navercorp.pinpoint.collector.receiver.DispatchHandler;
import com.navercorp.pinpoint.collector.service.async.AgentEventAsyncTaskService;
import com.navercorp.pinpoint.collector.service.async.AgentLifeCycleAsyncTaskService;
import com.navercorp.pinpoint.rpc.server.ChannelPropertiesFactory;
import com.navercorp.pinpoint.rpc.server.PinpointServerAcceptor;
import com.navercorp.pinpoint.rpc.server.ServerMessageListenerFactory;
import com.navercorp.pinpoint.rpc.server.handler.ServerStateChangeEventHandler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.annotation.Resource;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.Executor;

/**
 * @author emeroad
 * @author koo.taejin
 */
public class AgentBaseDataReceiver {

    private final Logger logger = LoggerFactory.getLogger(AgentBaseDataReceiver.class);

    private final PinpointServerAcceptor acceptor;

    private final AgentBaseDataReceiverConfiguration configuration;

    private final ZookeeperClusterService clusterService;

    private final Executor executor;

    private final TCPPacketHandlerFactory tcpPacketHandlerFactory;

    private final TCPPacketHandler tcpPacketHandler;

    @Autowired
    private AgentEventAsyncTaskService agentEventAsyncTaskService;

    @Autowired
    private AgentLifeCycleAsyncTaskService agentLifeCycleAsyncTaskService;

    @Autowired
    private ChannelPropertiesFactory channelPropertiesFactory;

    @Resource(name = "channelStateChangeEventHandlers")
    private List<ServerStateChangeEventHandler> channelStateChangeEventHandlers = Collections.emptyList();

    public AgentBaseDataReceiver(AgentBaseDataReceiverConfiguration configuration, Executor executor, PinpointServerAcceptor acceptor, DispatchHandler dispatchHandler) {
        this(configuration, executor, acceptor, dispatchHandler, null);
    }

    public AgentBaseDataReceiver(AgentBaseDataReceiverConfiguration configuration, Executor executor, PinpointServerAcceptor acceptor, DispatchHandler dispatchHandler, ZookeeperClusterService service) {
        this(configuration, executor, acceptor, new DefaultTCPPacketHandlerFactory(), dispatchHandler, service);
    }

    public AgentBaseDataReceiver(AgentBaseDataReceiverConfiguration configuration, Executor executor, PinpointServerAcceptor acceptor, TCPPacketHandlerFactory tcpPacketHandlerFactory, DispatchHandler dispatchHandler, ZookeeperClusterService service) {
        this.configuration = Objects.requireNonNull(configuration, "config");
        this.executor = Objects.requireNonNull(executor, "executor");
        this.acceptor = Objects.requireNonNull(acceptor, "acceptor");

        this.tcpPacketHandlerFactory = Objects.requireNonNull(tcpPacketHandlerFactory, "tcpPacketHandlerFactory");
        this.tcpPacketHandler = wrapDispatchHandler(dispatchHandler);
        this.clusterService = service;
    }

    private TCPPacketHandler wrapDispatchHandler(DispatchHandler dispatchHandler) {
        Objects.requireNonNull(dispatchHandler, "dispatchHandler");
        return tcpPacketHandlerFactory.build(dispatchHandler);
    }

    @PostConstruct
    public void start() {
        if (logger.isInfoEnabled()) {
            logger.info("start() started");
        }

        prepare(acceptor);

        // take care when attaching message handlers as events are generated from the IO thread.
        // pass them to a separate queue and handle them in a different thread.
        ServerMessageListenerFactory messageListenerFactory = new AgentBaseDataReceiverServerMessageListenerFactory(
                executor, tcpPacketHandler,
                agentEventAsyncTaskService, agentLifeCycleAsyncTaskService,
                channelPropertiesFactory);
        acceptor.setMessageListenerFactory(messageListenerFactory);
        acceptor.bind(configuration.getBindIp(), configuration.getBindPort());

        if (logger.isInfoEnabled()) {
            logger.info("start() completed");
        }
    }

    private void prepare(PinpointServerAcceptor acceptor) {
        if (clusterService != null && clusterService.isEnable()) {
            ProfilerClusterManager profilerClusterManager = clusterService.getProfilerClusterManager();
            final ServerStateChangeEventHandler stateChangeEventHandler = new ClusterPointStateChangedEventHandler(channelPropertiesFactory, profilerClusterManager);

            logger.info("Add Cluster channel state change event handlers {}", stateChangeEventHandler);
            acceptor.addStateChangeEventHandler(stateChangeEventHandler);
        } else {
            logger.info("clusterService is disabled");
        }

        for (ServerStateChangeEventHandler channelStateChangeEventHandler : this.channelStateChangeEventHandlers) {
            logger.info("Add channel state change event handlers {}", channelStateChangeEventHandler);
            acceptor.addStateChangeEventHandler(channelStateChangeEventHandler);
        }
    }

    @PreDestroy
    public void stop() {
        if (logger.isInfoEnabled()) {
            logger.info("stop() started");
        }

        if (acceptor != null) {
            acceptor.close();
        }

        if (logger.isInfoEnabled()) {
            logger.info("stop() completed");
        }
    }
}

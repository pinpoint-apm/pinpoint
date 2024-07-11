/*
 * Copyright 2021 NAVER Corp.
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

package com.navercorp.pinpoint.collector.grpc.config;

import com.google.protobuf.GeneratedMessageV3;
import com.navercorp.pinpoint.collector.handler.RequestResponseHandler;
import com.navercorp.pinpoint.collector.handler.SimpleAndRequestResponseHandler;
import com.navercorp.pinpoint.collector.manage.HandlerManager;
import com.navercorp.pinpoint.collector.receiver.AgentDispatchHandler;
import com.navercorp.pinpoint.collector.receiver.AgentLifeCycleChangeEventHandler;
import com.navercorp.pinpoint.collector.receiver.DispatchHandler;
import com.navercorp.pinpoint.collector.receiver.DispatchHandlerFactoryBean;
import com.navercorp.pinpoint.collector.receiver.grpc.GrpcReceiver;
import com.navercorp.pinpoint.collector.receiver.grpc.ServerInterceptorFactory;
import com.navercorp.pinpoint.collector.receiver.grpc.ShutdownEventListener;
import com.navercorp.pinpoint.collector.receiver.grpc.SimpleServerCallExecutorSupplier;
import com.navercorp.pinpoint.collector.receiver.grpc.service.AgentLifecycleListener;
import com.navercorp.pinpoint.collector.receiver.grpc.service.AgentService;
import com.navercorp.pinpoint.collector.receiver.grpc.service.KeepAliveService;
import com.navercorp.pinpoint.collector.receiver.grpc.service.MetadataService;
import com.navercorp.pinpoint.collector.receiver.grpc.service.ServerRequestFactory;
import com.navercorp.pinpoint.collector.service.AgentInfoService;
import com.navercorp.pinpoint.collector.service.async.AgentEventAsyncTaskService;
import com.navercorp.pinpoint.collector.service.async.AgentLifeCycleAsyncTaskService;
import com.navercorp.pinpoint.common.server.util.AcceptedTimeService;
import com.navercorp.pinpoint.common.server.util.IgnoreAddressFilter;
import com.navercorp.pinpoint.grpc.server.lifecycle.DefaultPingEventHandler;
import com.navercorp.pinpoint.grpc.server.lifecycle.DefaultPingSessionRegistry;
import com.navercorp.pinpoint.grpc.server.lifecycle.PingEventHandler;
import com.navercorp.pinpoint.grpc.server.lifecycle.PingSessionRegistry;
import com.navercorp.pinpoint.rpc.server.ChannelPropertiesFactory;
import com.navercorp.pinpoint.rpc.server.handler.ServerStateChangeEventHandler;
import io.grpc.BindableService;
import io.grpc.ServerInterceptor;
import io.grpc.ServerServiceDefinition;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

import java.util.List;
import java.util.concurrent.Executor;

/**
 * @author emeroad
 */
@Configuration
public class GrpcAgentConfiguration {

    private static final Logger logger = LogManager.getLogger(GrpcAgentConfiguration.class);

    public GrpcAgentConfiguration() {
    }

    @Bean
    public AgentService agentService(@Qualifier("grpcDispatchHandlerFactoryBean")
                                     DispatchHandler<GeneratedMessageV3, GeneratedMessageV3> dispatchHandler,
                                     PingEventHandler pingEventHandler,
                                     @Qualifier("grpcAgentWorkerExecutor")
                                     Executor executor,
                                     ServerRequestFactory serverRequestFactory) {
        return new AgentService(dispatchHandler, pingEventHandler, executor, serverRequestFactory);
    }

    @Bean
    public MetadataService metadataService(@Qualifier("grpcDispatchHandlerFactoryBean")
                                           DispatchHandler<GeneratedMessageV3, GeneratedMessageV3> dispatchHandler,
                                           @Qualifier("grpcAgentWorkerExecutor")
                                           Executor executor,
                                           ServerRequestFactory serverRequestFactory) {
        return new MetadataService(dispatchHandler, executor, serverRequestFactory);
    }


    @Bean
    public List<ServerServiceDefinition> agentServiceList(AgentService agentService,
                                                          MetadataService metadataService,
                                                          @Qualifier("commandService")
                                                          BindableService grpcCommandService) {
        logger.info("AgentService:{}", agentService);
        logger.info("MetadataService:{}", metadataService);
        logger.info("CommandService:{}", grpcCommandService);
        return List.of(agentService.bindService(), metadataService.bindService(), grpcCommandService.bindService());
    }

    @Bean
    public SimpleServerCallExecutorSupplier grpcAgentServerCallExecutorSupplier(@Qualifier("grpcAgentServerCallExecutor")
                                                                                Executor grpcAgentServerCallExecutor) {
        return new SimpleServerCallExecutorSupplier(grpcAgentServerCallExecutor);
    }

    @Bean
    public GrpcReceiver grpcAgentReceiver(@Qualifier("grpcAgentReceiverProperties")
                                          GrpcReceiverProperties properties,
                                          IgnoreAddressFilter addressFilter,
                                          @Qualifier("agentServiceList")
                                          List<ServerServiceDefinition> spanServiceList,
                                          @Qualifier("agentInterceptorList")
                                          List<ServerInterceptor> spanInterceptorList,
                                          @Qualifier("grpcAgentServerExecutor")
                                          Executor grpcSpanExecutor,
                                          @Qualifier("grpcAgentServerCallExecutorSupplier")
                                          SimpleServerCallExecutorSupplier simpleServerCallExecutorSupplier) {
        GrpcReceiver grpcReceiver = new GrpcReceiver();
        grpcReceiver.setBindAddress(properties.getBindAddress());
        grpcReceiver.setAddressFilter(addressFilter);
        grpcReceiver.setBindableServiceList(spanServiceList);
        grpcReceiver.setServerInterceptorList(spanInterceptorList);

        grpcReceiver.setExecutor(grpcSpanExecutor);
        grpcReceiver.setEnable(properties.isEnable());
        grpcReceiver.setServerOption(properties.getServerOption());

        grpcReceiver.setServerCallExecutorSupplier(simpleServerCallExecutorSupplier);
        return grpcReceiver;
    }


    @Bean
    public AgentDispatchHandler<GeneratedMessageV3, GeneratedMessageV3> grpcAgentDispatchHandler(
            @Qualifier("grpcAgentInfoHandler")
            SimpleAndRequestResponseHandler<GeneratedMessageV3, GeneratedMessageV3> agentInfoHandler,
            List<RequestResponseHandler<GeneratedMessageV3, GeneratedMessageV3>> handlers) {
        return new AgentDispatchHandler<>(agentInfoHandler, handlers);
    }

    @Bean
    public FactoryBean<DispatchHandler<GeneratedMessageV3, GeneratedMessageV3>> grpcDispatchHandlerFactoryBean(
            AgentDispatchHandler<GeneratedMessageV3, GeneratedMessageV3> dispatchHandler,
            HandlerManager handlerManager,
            AcceptedTimeService acceptedTimeService) {
        DispatchHandlerFactoryBean<GeneratedMessageV3, GeneratedMessageV3> bean = new DispatchHandlerFactoryBean<>();
        bean.setDispatchHandler(dispatchHandler);
        bean.setHandlerManager(handlerManager);
        bean.setAcceptedTimeService(acceptedTimeService);
        return bean;
    }


    @Bean
    public ThreadPoolTaskScheduler grpcLifecycleScheduler() {
        ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
        scheduler.setPoolSize(1);
        scheduler.setThreadNamePrefix("Pinpoint-GrpcLifecycleFlusher-");
        scheduler.setDaemon(true);
        scheduler.setWaitForTasksToCompleteOnShutdown(true);
        scheduler.setAwaitTerminationSeconds(10);
        return scheduler;
    }

    @Bean
    public PingSessionRegistry pingSessionRegistry() {
        return new DefaultPingSessionRegistry();
    }

    @Bean
    public AgentLifecycleListener lifecycleListener(KeepAliveService lifecycleService,
                                                    AgentInfoService agentInfoService,
                                                    ShutdownEventListener shutdownEventListener) {
        return new AgentLifecycleListener(lifecycleService, agentInfoService, shutdownEventListener);
    }

    @Bean
    public KeepAliveService keepAliveService(AgentEventAsyncTaskService agentEventAsyncTask,
                                             AgentLifeCycleAsyncTaskService agentLifeCycleAsyncTask,
                                             PingSessionRegistry pingSessionRegistry) {
        return new KeepAliveService(agentEventAsyncTask, agentLifeCycleAsyncTask, pingSessionRegistry);
    }

    @Bean
    public PingEventHandler pingEventHandler(PingSessionRegistry pingSessionRegistry,
                                                   AgentLifecycleListener agentLifecycleListener) {
        return new DefaultPingEventHandler(pingSessionRegistry, agentLifecycleListener);
    }

    @Bean
    public ShutdownEventListener shutdownEventListener() {
        return new ShutdownEventListener();
    }

    @Bean
    public AgentLifeCycleChangeEventHandler agentLifeCycleChangeEventHandler(AgentLifeCycleAsyncTaskService agentLifeCycleAsyncTaskService,
                                                                             AgentEventAsyncTaskService agentEventAsyncTaskService,
                                                                             ChannelPropertiesFactory channelPropertiesFactory) {
        return new AgentLifeCycleChangeEventHandler(agentLifeCycleAsyncTaskService, agentEventAsyncTaskService, channelPropertiesFactory);
    }

    @Bean
    public List<ServerStateChangeEventHandler> channelStateChangeEventHandlers(AgentLifeCycleChangeEventHandler handler) {
        return List.of(handler);
    }

    @Bean
    public List<ServerInterceptor> agentInterceptorList() {
        return List.of(ServerInterceptorFactory.headerReader("agent"));
    }
}
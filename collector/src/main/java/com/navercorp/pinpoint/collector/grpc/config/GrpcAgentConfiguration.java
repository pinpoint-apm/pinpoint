/*
 * Copyright 2025 NAVER Corp.
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

import com.navercorp.pinpoint.collector.grpc.lifecycle.DefaultPingEventHandler;
import com.navercorp.pinpoint.collector.grpc.lifecycle.DefaultPingSessionRegistry;
import com.navercorp.pinpoint.collector.grpc.lifecycle.PingEventHandler;
import com.navercorp.pinpoint.collector.grpc.lifecycle.PingSessionRegistry;
import com.navercorp.pinpoint.collector.handler.RequestResponseHandler;
import com.navercorp.pinpoint.collector.receiver.grpc.GrpcReceiver;
import com.navercorp.pinpoint.collector.receiver.grpc.ServerInterceptorFactory;
import com.navercorp.pinpoint.collector.receiver.grpc.ShutdownEventListener;
import com.navercorp.pinpoint.collector.receiver.grpc.SimpleServerCallExecutorSupplier;
import com.navercorp.pinpoint.collector.receiver.grpc.monitor.Monitor;
import com.navercorp.pinpoint.collector.receiver.grpc.service.AgentLifecycleListener;
import com.navercorp.pinpoint.collector.receiver.grpc.service.AgentService;
import com.navercorp.pinpoint.collector.receiver.grpc.service.KeepAliveService;
import com.navercorp.pinpoint.collector.receiver.grpc.service.MetadataService;
import com.navercorp.pinpoint.collector.receiver.grpc.service.ServerRequestFactory;
import com.navercorp.pinpoint.collector.receiver.grpc.service.ServerResponseFactory;
import com.navercorp.pinpoint.collector.service.async.AgentEventAsyncTaskService;
import com.navercorp.pinpoint.collector.service.async.AgentLifeCycleAsyncTaskService;
import com.navercorp.pinpoint.common.server.uid.ObjectNameVersion;
import com.navercorp.pinpoint.common.server.util.IgnoreAddressFilter;
import com.navercorp.pinpoint.grpc.trace.PAgentInfo;
import com.navercorp.pinpoint.grpc.trace.PApiMetaData;
import com.navercorp.pinpoint.grpc.trace.PExceptionMetaData;
import com.navercorp.pinpoint.grpc.trace.PResult;
import com.navercorp.pinpoint.grpc.trace.PSqlMetaData;
import com.navercorp.pinpoint.grpc.trace.PSqlUidMetaData;
import com.navercorp.pinpoint.grpc.trace.PStringMetaData;
import io.grpc.BindableService;
import io.grpc.ServerInterceptor;
import io.netty.buffer.ByteBufAllocator;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
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
    public AgentService agentService(RequestResponseHandler<PAgentInfo, PResult> grpcAgentInfoHandler,
                                     PingEventHandler pingEventHandler,
                                     @Qualifier("grpcAgentWorkerExecutor")
                                     Executor executor,
                                     ServerRequestFactory serverRequestFactory,
                                     ServerResponseFactory serverResponseFactory) {
        return new AgentService(grpcAgentInfoHandler, pingEventHandler, executor, serverRequestFactory, serverResponseFactory);
    }

    @Bean
    public MetadataService metadataService(RequestResponseHandler<PApiMetaData, PResult> grpcApiMetaDataHandler,
                                           RequestResponseHandler<PSqlMetaData, PResult> grpcSqlMetaDataHandler,
                                           RequestResponseHandler<PStringMetaData, PResult> grpcStringMetaDataHandler,
                                           RequestResponseHandler<PSqlUidMetaData, PResult> grpcSqlUidMetaDataHandler,
                                           RequestResponseHandler<PExceptionMetaData, PResult> grpcExceptionMetaDataHandler,
                                           @Qualifier("grpcAgentWorkerExecutor")
                                           Executor executor,
                                           ServerRequestFactory serverRequestFactory,
                                           ServerResponseFactory serverResponseFactory) {

        return new MetadataService(
                grpcApiMetaDataHandler,
                grpcSqlMetaDataHandler,
                grpcSqlUidMetaDataHandler,
                grpcStringMetaDataHandler,
                grpcExceptionMetaDataHandler,
                executor,
                serverRequestFactory, serverResponseFactory);
    }


    @Bean
    public ServerServiceDefinitions agentServiceList(AgentService agentService,
                                                     MetadataService metadataService,
                                                     @Qualifier("commandService")
                                                     BindableService grpcCommandService) {
        logger.info("AgentService:{}", agentService);
        logger.info("MetadataService:{}", metadataService);
        logger.info("CommandService:{}", grpcCommandService);

        return ServerServiceDefinitions.of(agentService.bindService(), metadataService.bindService(), grpcCommandService.bindService());

    }

    @Bean
    public SimpleServerCallExecutorSupplier grpcAgentServerCallExecutorSupplier(@Qualifier("grpcAgentServerCallExecutor")
                                                                                Executor grpcAgentServerCallExecutor) {
        return new SimpleServerCallExecutorSupplier(grpcAgentServerCallExecutor);
    }

    @Bean
    public GrpcReceiver grpcAgentReceiver(@Qualifier("grpcAgentReceiverProperties")
                                          GrpcReceiverProperties properties,
                                          @Qualifier("monitoredByteBufAllocator") ByteBufAllocator byteBufAllocator,
                                          IgnoreAddressFilter addressFilter,
                                          @Qualifier("agentServiceList")
                                          ServerServiceDefinitions spanServices,
                                          @Qualifier("agentInterceptor")
                                          List<ServerInterceptor> spanInterceptorList,
                                          @Qualifier("grpcAgentServerExecutor")
                                          Executor grpcSpanExecutor,
                                          @Qualifier("grpcAgentServerCallExecutorSupplier")
                                          SimpleServerCallExecutorSupplier simpleServerCallExecutorSupplier,
                                          Monitor monitor) {
        GrpcReceiver grpcReceiver = new GrpcReceiver();
        grpcReceiver.setBindAddress(properties.getBindAddress());
        grpcReceiver.setAddressFilter(addressFilter);
        grpcReceiver.setBindableServiceList(spanServices.getDefinitions());
        grpcReceiver.setServerInterceptorList(spanInterceptorList);

        grpcReceiver.setExecutor(grpcSpanExecutor);
        grpcReceiver.setEnable(properties.isEnable());
        grpcReceiver.setServerOption(properties.getServerOption());
        grpcReceiver.setByteBufAllocator(byteBufAllocator);

        grpcReceiver.setServerCallExecutorSupplier(simpleServerCallExecutorSupplier);

        grpcReceiver.setMonitor(monitor);
        return grpcReceiver;
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
                                                    ShutdownEventListener shutdownEventListener) {
        return new AgentLifecycleListener(lifecycleService, shutdownEventListener);
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
    @Qualifier("agentInterceptor")
    public ServerInterceptor agentInterceptorList(ObjectNameVersion version) {
        ServerInterceptorFactory serverInterceptorFactory = new ServerInterceptorFactory("agent");
        serverInterceptorFactory.applyVersion(version);
        return serverInterceptorFactory.build();
    }
}
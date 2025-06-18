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

package com.navercorp.pinpoint.collector.receiver.grpc;

import com.navercorp.pinpoint.collector.handler.SimpleHandler;
import com.navercorp.pinpoint.collector.receiver.BindAddress;
import com.navercorp.pinpoint.collector.receiver.grpc.flow.RateLimitClientStreamServerInterceptor;
import com.navercorp.pinpoint.collector.receiver.grpc.service.DefaultServerRequestFactory;
import com.navercorp.pinpoint.collector.receiver.grpc.service.ServerRequestFactory;
import com.navercorp.pinpoint.collector.receiver.grpc.service.SpanService;
import com.navercorp.pinpoint.collector.receiver.grpc.service.StreamCloseOnError;
import com.navercorp.pinpoint.common.server.uid.ApplicationUid;
import com.navercorp.pinpoint.common.server.util.AddressFilter;
import com.navercorp.pinpoint.grpc.server.HeaderPropagationInterceptor;
import com.navercorp.pinpoint.grpc.server.ServerHeaderReaderFactory;
import com.navercorp.pinpoint.grpc.server.ServerOption;
import com.navercorp.pinpoint.grpc.trace.PSpan;
import com.navercorp.pinpoint.grpc.trace.PSpanChunk;
import com.navercorp.pinpoint.io.request.ServerRequest;
import com.navercorp.pinpoint.io.request.UidFetcher;
import com.navercorp.pinpoint.io.request.UidFetcherStreamService;
import io.github.bucket4j.Bandwidth;
import io.grpc.ServerInterceptors;
import io.grpc.ServerServiceDefinition;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.net.InetAddress;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @author jaehong.kim
 */
public class SpanServerTestMain {
    private final Logger logger = LogManager.getLogger(this.getClass());

    public static final String IP = "0.0.0.0";
    public static final int PORT = 9993;

    public void run() throws Exception {

        GrpcReceiver grpcReceiver = new GrpcReceiver();
        grpcReceiver.setBeanName("TraceServer");
        BindAddress.Builder builder = BindAddress.newBuilder();
        builder.setIp(IP);
        builder.setPort(PORT);
        grpcReceiver.setBindAddress(builder.build());

        Executor executor = newWorkerExecutor(8);
        ServerServiceDefinition bindableService = newSpanBindableService(executor);
        grpcReceiver.setBindableServiceList(List.of(bindableService));
        grpcReceiver.setAddressFilter(new MockAddressFilter());
        grpcReceiver.setExecutor(Executors.newFixedThreadPool(8));
        grpcReceiver.setEnable(true);
        grpcReceiver.setServerOption(ServerOption.newBuilder().build());

        ServerHeaderReaderFactory agentHeaderReader = new ServerHeaderReaderFactory("test");
        HeaderPropagationInterceptor interceptor = new HeaderPropagationInterceptor(agentHeaderReader);
        grpcReceiver.setServerInterceptorList(List.of(interceptor));

//        for(int i = 0; i < 9999; i++) {
        grpcReceiver.afterPropertiesSet();

        grpcReceiver.blockUntilShutdown();
//            TimeUnit.SECONDS.sleep(30);
//            System.out.println("###### SHUTDOWN");
//            grpcReceiver.destroy();
//            grpcReceiver.blockUntilShutdown();
//            System.out.println("###### START");
//            TimeUnit.SECONDS.sleep(30);

    }

    private ServerServiceDefinition newSpanBindableService(Executor executor) {

        Bandwidth bandwidth = Bandwidth.builder().capacity(1000).refillGreedy(200, Duration.ofSeconds(1)).build();
        RateLimitClientStreamServerInterceptor rateLimit = new RateLimitClientStreamServerInterceptor("test-span", executor, bandwidth, 1);

        SimpleHandler<PSpan> handler1 = new MockSimpleHandler<>();
        SimpleHandler<PSpanChunk> handler2 = new MockSimpleHandler<>();
        ServerRequestFactory serverRequestFactory = new DefaultServerRequestFactory();

        UidFetcherStreamService uidFetcherStreamService = mock(UidFetcherStreamService.class);
        UidFetcher uidFetcher = mock(UidFetcher.class);
        when(uidFetcherStreamService.newUidFetcher()).thenReturn(uidFetcher);
        when(uidFetcher.getApplicationId(any(), any())).thenReturn(() -> ApplicationUid.of(100));

        SpanService spanService = new SpanService(handler1, handler2, uidFetcherStreamService, serverRequestFactory, StreamCloseOnError.FALSE);
        return ServerInterceptors.intercept(spanService, rateLimit);
    }

    private ExecutorService newWorkerExecutor(int thread) {
        return new ThreadPoolExecutor(thread, thread,
                0L, TimeUnit.MILLISECONDS,
                new LinkedBlockingQueue<>(thread * 2));
    }

    public static void main(String[] args) throws Exception {
        SpanServerTestMain main = new SpanServerTestMain();
        main.run();
    }

    private static class MockSimpleHandler<T> implements SimpleHandler<T> {
        private static final AtomicInteger counter = new AtomicInteger(0);

        @Override
        public void handleSimple(ServerRequest<T> serverRequest) {
//            System.out.println("## Incoming " + IncomingCounter.addAndGet(1));
            try {
                TimeUnit.SECONDS.sleep(1);
            } catch (InterruptedException ignored) {
            }

            System.out.println("Dispatch send message " + serverRequest.getData());
        }
    }

    private static class MockAddressFilter implements AddressFilter {
        @Override
        public boolean accept(InetAddress address) {
            return true;
        }
    }
}
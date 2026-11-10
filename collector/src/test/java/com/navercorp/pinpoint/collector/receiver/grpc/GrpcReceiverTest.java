/*
 * Copyright 2026 NAVER Corp.
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

package com.navercorp.pinpoint.collector.receiver.grpc;

import com.navercorp.pinpoint.collector.receiver.BindAddress;
import com.navercorp.pinpoint.collector.receiver.grpc.monitor.Monitor;
import com.navercorp.pinpoint.grpc.server.ServerOption;
import io.grpc.ServerServiceDefinition;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.SmartLifecycle;
import org.springframework.context.support.GenericApplicationContext;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class GrpcReceiverTest {

    private static final String LOCALHOST = "127.0.0.1";
    private static final String BEAN_NAME = "TestServer";

    @Test
    void portIsOpenedOnStartAndClosedOnStop() throws Exception {
        final int port = findAvailablePort();
        final ExecutorService executor = Executors.newFixedThreadPool(2);
        final GrpcReceiver receiver = newReceiver(port, executor, true);
        try {
            assertThat(receiver.isRunning()).isFalse();
            assertThat(isListening(port)).isFalse();

            receiver.start();
            assertThat(receiver.isRunning()).isTrue();
            assertThat(isListening(port)).isTrue();

            receiver.stop();
            assertThat(receiver.isRunning()).isFalse();
            assertThat(isListening(port)).isFalse();
        } finally {
            receiver.stop();
            executor.shutdownNow();
        }
    }

    @Test
    void startFailureReleasesResources() throws Exception {
        final ExecutorService executor = Executors.newFixedThreadPool(2);
        final AtomicInteger monitorClosed = new AtomicInteger();
        // keep the port occupied so that the bind fails
        try (ServerSocket occupied = new ServerSocket(0, 1, InetAddress.getByName(LOCALHOST))) {
            final GrpcReceiver receiver = newReceiver(occupied.getLocalPort(), executor, true);
            receiver.setMonitor(new Monitor() {
                @Override
                public void register(Runnable job) {
                }

                @Override
                public void close() {
                    monitorClosed.incrementAndGet();
                }
            });

            assertThatThrownBy(receiver::start).isInstanceOf(IllegalStateException.class);

            assertThat(receiver.isRunning()).isFalse();
            assertThat(monitorClosed).hasValue(1);
            assertThat(liveChannelThreads()).isEmpty();
        } finally {
            executor.shutdownNow();
        }
    }

    @Test
    void disabledReceiverDoesNotStart() throws Exception {
        final int port = findAvailablePort();
        final ExecutorService executor = Executors.newFixedThreadPool(2);
        final GrpcReceiver receiver = newReceiver(port, executor, false);
        try {
            receiver.start();

            assertThat(receiver.isRunning()).isFalse();
            assertThat(isListening(port)).isFalse();
        } finally {
            receiver.stop();
            executor.shutdownNow();
        }
    }

    @Test
    void contextStartsReceiverAfterSingletonsAndStopsItBeforeDestroy() throws Exception {
        final int port = findAvailablePort();
        final ExecutorService executor = Executors.newFixedThreadPool(2);
        final AtomicBoolean listeningOnInit = new AtomicBoolean(true);
        final AtomicBoolean listeningOnDestroy = new AtomicBoolean(true);

        final GenericApplicationContext context = new GenericApplicationContext();
        context.registerBean("receiver", GrpcReceiver.class, () -> newReceiver(port, executor, true));
        // created after the receiver bean, destroyed before it
        context.registerBean("probe", PortProbe.class, () -> new PortProbe(port, listeningOnInit, listeningOnDestroy));
        try {
            context.refresh();

            final GrpcReceiver receiver = context.getBean(GrpcReceiver.class);
            assertThat(listeningOnInit).isFalse();
            assertThat(receiver.isRunning()).isTrue();
            assertThat(isListening(port)).isTrue();
        } finally {
            context.close();
            executor.shutdownNow();
        }
        assertThat(listeningOnDestroy).isFalse();
        assertThat(isListening(port)).isFalse();
    }

    @Test
    void failedRefreshClosesStartedReceiver() throws Exception {
        final int port = findAvailablePort();
        final ExecutorService executor = Executors.newFixedThreadPool(2);

        final GenericApplicationContext context = new GenericApplicationContext();
        context.registerBean("receiver", GrpcReceiver.class, () -> newReceiver(port, executor, true));
        // depends on the receiver, so it is started after the receiver
        context.registerBean("failing", FailingLifecycle.class, FailingLifecycle::new, bd -> bd.setDependsOn("receiver"));
        try {
            assertThatThrownBy(context::refresh).hasRootCauseMessage("start failed");
            assertThat(isListening(port)).isFalse();
        } finally {
            context.close();
            executor.shutdownNow();
        }
    }

    private static class FailingLifecycle implements SmartLifecycle {
        @Override
        public void start() {
            throw new IllegalStateException("start failed");
        }

        @Override
        public void stop() {
        }

        @Override
        public boolean isRunning() {
            return false;
        }
    }

    private static class PortProbe implements InitializingBean, DisposableBean {
        private final int port;
        private final AtomicBoolean listeningOnInit;
        private final AtomicBoolean listeningOnDestroy;

        PortProbe(int port, AtomicBoolean listeningOnInit, AtomicBoolean listeningOnDestroy) {
            this.port = port;
            this.listeningOnInit = listeningOnInit;
            this.listeningOnDestroy = listeningOnDestroy;
        }

        @Override
        public void afterPropertiesSet() {
            this.listeningOnInit.set(isListening(port));
        }

        @Override
        public void destroy() {
            this.listeningOnDestroy.set(isListening(port));
        }
    }

    private GrpcReceiver newReceiver(int port, ExecutorService executor, boolean enable) {
        final GrpcReceiver receiver = new GrpcReceiver();
        receiver.setBeanName(BEAN_NAME);

        final BindAddress.Builder builder = BindAddress.newBuilder();
        builder.setIp(LOCALHOST);
        builder.setPort(port);
        receiver.setBindAddress(builder.build());

        final ServerServiceDefinition emptyService = ServerServiceDefinition.builder("test.EmptyService").build();
        receiver.setBindableServiceList(List.of(emptyService));
        receiver.setAddressFilter(address -> true);
        receiver.setExecutor(executor);
        receiver.setEnable(enable);
        receiver.setServerOption(ServerOption.newBuilder().build());
        return receiver;
    }

    private static List<String> liveChannelThreads() {
        return Thread.getAllStackTraces().keySet().stream()
                .filter(Thread::isAlive)
                .map(Thread::getName)
                .filter(name -> name.contains(BEAN_NAME + "-Channel"))
                .toList();
    }

    private static int findAvailablePort() throws IOException {
        try (ServerSocket socket = new ServerSocket(0)) {
            return socket.getLocalPort();
        }
    }

    private static boolean isListening(int port) {
        try (Socket socket = new Socket()) {
            socket.connect(new InetSocketAddress(LOCALHOST, port), 1000);
            return true;
        } catch (IOException e) {
            return false;
        }
    }
}

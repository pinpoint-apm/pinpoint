/*
 * Copyright 2023 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.navercorp.pinpoint.collector.service;

import com.google.common.util.concurrent.ListenableFuture;
import io.grpc.InternalChannelz.ServerStats;
import io.grpc.InternalChannelz.SocketOptions;
import io.grpc.InternalChannelz.SocketStats;
import io.grpc.InternalChannelz.TransportStats;
import io.grpc.InternalInstrumented;
import io.grpc.InternalLogId;

import javax.annotation.Nonnull;
import java.net.InetSocketAddress;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;

/**
 * @author youngjin.kim2
 */
public class ChannelzTestUtils {

    static SocketStats mockSocketStats(String remoteAddr, int localPort) {
        return new SocketStats(
                mockTransportStats(),
                new InetSocketAddress("127.0.0.1", localPort),
                new InetSocketAddress(remoteAddr, 2345),
                new SocketOptions.Builder().build(),
                null
        );
    }

    static ServerStats mockServerStats(List<InternalInstrumented<SocketStats>> sockets) {
        return new ServerStats(
                0,
                0,
                0,
                0,
                sockets
        );
    }

    static TransportStats mockTransportStats() {
        return new TransportStats(
                0,
                0,
                0,
                0,
                0,
                0,
                0,
                0,
                0,
                0,
                0,
                0
        );
    }

    static class SimpleInternalInstrumented<T> implements InternalInstrumented<T> {
        private final T stats;
        private final InternalLogId logId;

        public SimpleInternalInstrumented(T stats, InternalLogId logId) {
            this.stats = stats;
            this.logId = logId;
        }

        @Override
        public ListenableFuture<T> getStats() {
            return new ImmediateFuture<>(stats);
        }

        @Override
        public InternalLogId getLogId() {
            return logId;
        }
    }

    static class ImmediateFuture<T> implements ListenableFuture<T> {
        private final T result;

        public ImmediateFuture(T result) {
            this.result = result;
        }

        @Override
        public void addListener(@Nonnull Runnable listener, Executor executor) {
            executor.execute(listener);
        }

        @Override
        public boolean cancel(boolean mayInterruptIfRunning) {
            return false;
        }

        @Override
        public boolean isCancelled() {
            return false;
        }

        @Override
        public boolean isDone() {
            return true;
        }

        @Override
        public T get() {
            return result;
        }

        @Override
        public T get(long timeout, @Nonnull TimeUnit unit) {
            return result;
        }
    }

}

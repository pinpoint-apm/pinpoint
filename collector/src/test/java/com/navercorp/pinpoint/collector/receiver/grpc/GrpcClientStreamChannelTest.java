/*
 * Copyright 2019 NAVER Corp.
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

import com.google.protobuf.Empty;
import com.navercorp.pinpoint.rpc.packet.stream.StreamClosePacket;
import com.navercorp.pinpoint.rpc.packet.stream.StreamCode;
import com.navercorp.pinpoint.rpc.packet.stream.StreamResponsePacket;
import com.navercorp.pinpoint.rpc.stream.ClientStreamChannel;
import com.navercorp.pinpoint.rpc.stream.ClientStreamChannelEventHandler;
import com.navercorp.pinpoint.rpc.stream.StreamChannelRepository;
import com.navercorp.pinpoint.rpc.stream.StreamChannelStateCode;
import com.navercorp.pinpoint.rpc.stream.StreamException;
import io.grpc.stub.StreamObserver;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.net.InetSocketAddress;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author Taejin Koo
 */
public class GrpcClientStreamChannelTest {

    private final InetSocketAddress mockRemoteAddress = new InetSocketAddress("127.0.0.1", 61611);

    @Test
    public void connectTimeoutTest() throws StreamException {
        GrpcClientStreamChannel grpcClientStreamChannel = new GrpcClientStreamChannel(mockRemoteAddress, 20, new StreamChannelRepository(), ClientStreamChannelEventHandler.DISABLED_INSTANCE);

        grpcClientStreamChannel.init();

        StreamException streamException = null;
        try {
            grpcClientStreamChannel.connect(new Runnable() {
                @Override
                public void run() {

                }
            }, 500);
        } catch (StreamException e) {
            streamException = e;
        }

        Assertions.assertEquals(StreamCode.CONNECTION_TIMEOUT, streamException.getStreamCode());
        Assertions.assertEquals(StreamChannelStateCode.CLOSED, grpcClientStreamChannel.getCurrentState());
    }

    @Test
    public void connectFailTest() throws StreamException {
        GrpcClientStreamChannel grpcClientStreamChannel = new GrpcClientStreamChannel(mockRemoteAddress, 20, new StreamChannelRepository(), ClientStreamChannelEventHandler.DISABLED_INSTANCE);
        grpcClientStreamChannel.init();

        StreamException streamException = null;
        try {
            grpcClientStreamChannel.connect(new Runnable() {
                @Override
                public void run() {
                    throw new IllegalStateException("fail");
                }
            }, 500);
        } catch (StreamException e) {
            streamException = e;
        }

        Assertions.assertEquals(StreamCode.CONNECTION_ERRROR, streamException.getStreamCode());
        Assertions.assertEquals(StreamChannelStateCode.CLOSED, grpcClientStreamChannel.getCurrentState());
    }

    @Test
    public void simpleTest() throws StreamException, InterruptedException {
        StreamChannelRepository streamChannelRepository = new StreamChannelRepository();

        RecordClientStreamChannelEventHandler recordStreamChannelHandler = new RecordClientStreamChannelEventHandler();
        GrpcClientStreamChannel grpcClientStreamChannel = new GrpcClientStreamChannel(mockRemoteAddress, 20, streamChannelRepository, recordStreamChannelHandler);
        Assertions.assertEquals(StreamChannelStateCode.NEW, grpcClientStreamChannel.getCurrentState());

        grpcClientStreamChannel.init();
        Assertions.assertEquals(StreamChannelStateCode.OPEN, grpcClientStreamChannel.getCurrentState());
        Assertions.assertEquals(1, streamChannelRepository.size());

        CountDownLatch connectCompleteLatch = new CountDownLatch(1);
        CountDownLatch threadCompleteLatch = connect(grpcClientStreamChannel, connectCompleteLatch);

        final AtomicInteger callCompletedCount = new AtomicInteger(0);
        grpcClientStreamChannel.setConnectionObserver(new StreamObserver<Empty>() {
            @Override
            public void onNext(Empty value) {

            }

            @Override
            public void onError(Throwable t) {

            }

            @Override
            public void onCompleted() {
                callCompletedCount.incrementAndGet();
            }
        });

        Assertions.assertTrue(connectCompleteLatch.await(1000, TimeUnit.MILLISECONDS));
        Assertions.assertEquals(StreamChannelStateCode.CONNECT_AWAIT, grpcClientStreamChannel.getCurrentState());

        grpcClientStreamChannel.changeStateConnected();
        Assertions.assertEquals(StreamChannelStateCode.CONNECTED, grpcClientStreamChannel.getCurrentState());

        Assertions.assertTrue(threadCompleteLatch.await(1000, TimeUnit.MILLISECONDS));

        String message = "hello";
        grpcClientStreamChannel.handleStreamResponsePacket(new StreamResponsePacket(1, message.getBytes()));
        Assertions.assertEquals(message, new String(recordStreamChannelHandler.getLastStreamResponsePacket().getPayload()));

        Assertions.assertEquals(0, callCompletedCount.get());

        grpcClientStreamChannel.handleStreamClosePacket(new StreamClosePacket(1, StreamCode.STATE_CLOSED));
        Assertions.assertEquals(0, streamChannelRepository.size());
        Assertions.assertEquals(StreamChannelStateCode.CLOSED, grpcClientStreamChannel.getCurrentState());
        Assertions.assertEquals(1, callCompletedCount.get());

        // do nothing
        grpcClientStreamChannel.disconnect();
        Assertions.assertEquals(1, callCompletedCount.get());
        grpcClientStreamChannel.close();
        Assertions.assertEquals(1, callCompletedCount.get());
    }

    private CountDownLatch connect(GrpcClientStreamChannel grpcClientStreamChannel, CountDownLatch connectCompleteLatch) {
        CountDownLatch threadCompleteLatch = new CountDownLatch(1);

        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    grpcClientStreamChannel.connect(new Runnable() {
                        @Override
                        public void run() {
                            connectCompleteLatch.countDown();
                        }
                    }, 1000);
                } catch (StreamException e) {
                    e.printStackTrace();
                } finally {
                    threadCompleteLatch.countDown();
                }
            }
        });
        thread.setDaemon(true);
        thread.start();
        return threadCompleteLatch;
    }

    private static class RecordClientStreamChannelEventHandler extends ClientStreamChannelEventHandler {

        private final Logger logger = LogManager.getLogger(this.getClass());

        private StreamResponsePacket lastStreamResponsePacket;

        @Override
        public void handleStreamResponsePacket(ClientStreamChannel streamChannel, StreamResponsePacket packet) {
            logger.info("handleStreamResponsePacket");
            this.lastStreamResponsePacket = packet;
        }

        StreamResponsePacket getLastStreamResponsePacket() {
            return lastStreamResponsePacket;
        }

        @Override
        public void handleStreamClosePacket(ClientStreamChannel streamChannel, StreamClosePacket packet) {
            logger.info("handleStreamClosePacket");
        }

        @Override
        public void stateUpdated(ClientStreamChannel streamChannel, StreamChannelStateCode updatedStateCode) {
            logger.info("stateUpdated streamChannel:{}, currentState:{}", streamChannel, updatedStateCode);
        }

    }


}

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

import com.navercorp.pinpoint.rpc.packet.stream.StreamClosePacket;
import com.navercorp.pinpoint.rpc.packet.stream.StreamCode;
import com.navercorp.pinpoint.rpc.packet.stream.StreamResponsePacket;
import com.navercorp.pinpoint.rpc.stream.AbstractStreamChannel;
import com.navercorp.pinpoint.rpc.stream.ClientStreamChannel;
import com.navercorp.pinpoint.rpc.stream.ClientStreamChannelEventHandler;
import com.navercorp.pinpoint.rpc.stream.StreamChannelRepository;
import com.navercorp.pinpoint.rpc.stream.StreamChannelStateChangeEventHandler;
import com.navercorp.pinpoint.rpc.stream.StreamChannelStateCode;
import com.navercorp.pinpoint.rpc.stream.StreamException;

import com.google.protobuf.Empty;
import io.grpc.Status;
import io.grpc.StatusException;
import io.grpc.stub.StreamObserver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;

/**
 * @author Taejin Koo
 */
public class GrpcClientStreamChannel extends AbstractStreamChannel implements ClientStreamChannel {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final AtomicReference<StreamObserver<Empty>> connectionObserverReference = new AtomicReference<>();

    private final InetSocketAddress remoteAddress;
    private final ClientStreamChannelEventHandler streamChannelEventHandler;

    public GrpcClientStreamChannel(InetSocketAddress remoteAddress, int streamId, StreamChannelRepository streamChannelRepository, ClientStreamChannelEventHandler streamChannelEventHandler) {
        super(streamId, streamChannelRepository);
        this.remoteAddress = Objects.requireNonNull(remoteAddress, "remoteAddress");
        this.streamChannelEventHandler = Objects.requireNonNull(streamChannelEventHandler, "streamChannelEventHandler");
    }

    public void connect(Runnable connectRunnable, long timeout) throws StreamException {
        changeStateTo(StreamChannelStateCode.CONNECT_AWAIT, true);

        try {
            connectRunnable.run();
        } catch (RuntimeException e) {
            changeStateTo(StreamChannelStateCode.CLOSED);
            throw new StreamException(StreamCode.CONNECTION_ERRROR, e.getMessage());
        }

        boolean connected = awaitOpen(timeout);
        if (connected) {
            logger.info("Open streamChannel initialization completed. streamChannel:{} ", this);
        } else {
            changeStateTo(StreamChannelStateCode.CLOSED);
            throw new StreamException(StreamCode.CONNECTION_TIMEOUT);
        }
    }

    // handle create success
    public boolean setConnectionObserver(StreamObserver<Empty> connectionObserver) {
        return connectionObserverReference.compareAndSet(null, connectionObserver);
    }

    @Override
    public boolean changeStateConnected() {
        if (connectionObserverReference.get() == null) {
            return false;
        }
        return super.changeStateConnected();
    }

    @Override
    public void sendPing(int requestId) {
        // not supported
    }

    @Override
    public void sendPong(int requestId) {
        // not supported
    }

    @Override
    public void close(StreamCode code) {
        logger.info("close. local => {}(streamId:{}, state:{}) message:{}", getRemoteAddress(), getStreamId(), getCurrentState(), code);

        if (getCurrentState() != StreamChannelStateCode.CLOSED) {
            clearStreamChannelResource();
            close0(code);
        }
    }

    @Override
    public void disconnect(StreamCode code) {
        logger.info("disconnect. local => {}(streamId:{}, state:{}) message:{}", getRemoteAddress(), getStreamId(), getCurrentState(), code);
        if (getCurrentState() != StreamChannelStateCode.CLOSED) {
            clearStreamChannelResource();
            close0(code);
        }
    }

    private void close0(StreamCode code) {
        StreamObserver<Empty> connectionObserver = connectionObserverReference.get();
        if (connectionObserver != null) {
            if (code == StreamCode.STATE_CLOSED) {
                Empty empty = Empty.newBuilder().build();
                connectionObserver.onNext(empty);
                connectionObserver.onCompleted();
            } else {
                Empty empty = Empty.newBuilder().build();
                connectionObserver.onNext(empty);
                connectionObserver.onError(new StatusException(Status.ABORTED.withDescription(code.name())));
            }
            connectionObserverReference.compareAndSet(connectionObserver, null);
        }
    }

    @Override
    public void handleStreamResponsePacket(StreamResponsePacket packet) throws StreamException {
        if (state.checkState(StreamChannelStateCode.CONNECTED)) {
            streamChannelEventHandler.handleStreamResponsePacket(this, packet);
        } else if (state.checkState(StreamChannelStateCode.CONNECT_AWAIT)) {
            // may happen in the timing
        } else {
            throw new StreamException(StreamCode.STATE_NOT_CONNECTED);
        }
    }

    @Override
    public void handleStreamClosePacket(StreamClosePacket packet) {
        streamChannelEventHandler.handleStreamClosePacket(this, packet);
        disconnect(packet.getCode());
    }

    @Override
    public SocketAddress getRemoteAddress() {
        return remoteAddress;
    }

    @Override
    protected StreamChannelStateChangeEventHandler getStateChangeEventHandler() {
        return streamChannelEventHandler;
    }

}



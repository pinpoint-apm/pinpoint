/*
 * Copyright 2014 NAVER Corp.
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

package com.navercorp.pinpoint.rpc.client;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.navercorp.pinpoint.rpc.DefaultFuture;
import com.navercorp.pinpoint.rpc.Future;
import com.navercorp.pinpoint.rpc.PinpointSocketException;
import com.navercorp.pinpoint.rpc.ResponseMessage;
import com.navercorp.pinpoint.rpc.stream.ClientStreamChannelContext;
import com.navercorp.pinpoint.rpc.stream.ClientStreamChannelMessageListener;
import com.navercorp.pinpoint.rpc.stream.StreamChannelContext;
import com.navercorp.pinpoint.rpc.util.AssertUtils;


/**
 * @author emeroad
 * @author koo.taejin
 * @author netspider
 */
public class PinpointSocket {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private volatile SocketHandler socketHandler;

    private volatile boolean closed;
    
    private List<PinpointSocketReconnectEventListener> reconnectEventListeners = new CopyOnWriteArrayList<PinpointSocketReconnectEventListener>();
    
    public PinpointSocket() {
        this(new ReconnectStateSocketHandler());
    }

    public PinpointSocket(SocketHandler socketHandler) {
        AssertUtils.assertNotNull(socketHandler, "socketHandler");
        socketHandler.doHandshake();

        this.socketHandler = socketHandler;
        socketHandler.setPinpointSocket(this);
    }

    void reconnectSocketHandler(SocketHandler socketHandler) {
        AssertUtils.assertNotNull(socketHandler, "socketHandler");

        if (closed) {
            logger.warn("reconnectSocketHandler(). socketHandler force close.");
            socketHandler.close();
            return;
        }
        logger.warn("reconnectSocketHandler:{}", socketHandler);
        
        // register listener before becoming internal object of Pinpoint socket.
        socketHandler.doHandshake();
        
        this.socketHandler = socketHandler;
        
        notifyReconnectEvent();
    }
    

    /*
        because reconnectEventListener's constructor contains Dummy and can't be access through setter,
        guarantee it is not null.
    */
    public boolean addPinpointSocketReconnectEventListener(PinpointSocketReconnectEventListener eventListener) {
        if (eventListener == null) {
            return false;
        }

        return this.reconnectEventListeners.add(eventListener);
    }

    public boolean removePinpointSocketReconnectEventListener(PinpointSocketReconnectEventListener eventListener) {
        if (eventListener == null) {
            return false;
        }

        return this.reconnectEventListeners.remove(eventListener);
    }

    private void notifyReconnectEvent() {
        for (PinpointSocketReconnectEventListener eachListener : this.reconnectEventListeners) {
            eachListener.reconnectPerformed(this);
        }
    }

    public void sendSync(byte[] bytes) {
        ensureOpen();
        socketHandler.sendSync(bytes);
    }

    public Future sendAsync(byte[] bytes) {
        ensureOpen();
        return socketHandler.sendAsync(bytes);
    }

    public void send(byte[] bytes) {
        ensureOpen();
        socketHandler.send(bytes);
    }


    public Future<ResponseMessage> request(byte[] bytes) {
        if (socketHandler == null) {
            return returnFailureFuture();
        }
        return socketHandler.request(bytes);
    }

    public ClientStreamChannelContext createStreamChannel(byte[] payload, ClientStreamChannelMessageListener clientStreamChannelMessageListener) {
        // StreamChannel must be changed into interface in order to throw the StreamChannel that returns failure.
        // fow now throw just exception
        ensureOpen();
        return socketHandler.createStreamChannel(payload, clientStreamChannelMessageListener);
    }
    
    public StreamChannelContext findStreamChannel(int streamChannelId) {

        ensureOpen();
        return socketHandler.findStreamChannel(streamChannelId);
    }

    private Future<ResponseMessage> returnFailureFuture() {
        DefaultFuture<ResponseMessage> future = new DefaultFuture<ResponseMessage>();
        future.setFailure(new PinpointSocketException("socketHandler is null"));
        return future;
    }

    private void ensureOpen() {
        if (socketHandler == null) {
            throw new PinpointSocketException("socketHandler is null");
        }
    }

    /**
     * write pinng packet on tcp channel
     * PinpointSocketException throws when writing fails.
     *
     */
    public void sendPing() {
        SocketHandler socketHandler = this.socketHandler;
        if (socketHandler == null) {
            return;
        }
        socketHandler.sendPing();
    }

    public void close() {
        synchronized (this) {
            if (closed) {
                return;
            }
            closed = true;
        }
        SocketHandler socketHandler = this.socketHandler;
        if (socketHandler == null) {
            return;
        }
        socketHandler.close();
    }

    public boolean isClosed() {
        return closed;
    }

    public boolean isConnected() {
        return this.socketHandler.isConnected();
    }
}

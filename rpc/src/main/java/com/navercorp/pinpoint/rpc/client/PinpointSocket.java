package com.nhn.pinpoint.rpc.client;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.nhn.pinpoint.rpc.DefaultFuture;
import com.nhn.pinpoint.rpc.Future;
import com.nhn.pinpoint.rpc.PinpointSocketException;
import com.nhn.pinpoint.rpc.ResponseMessage;
import com.nhn.pinpoint.rpc.stream.ClientStreamChannelContext;
import com.nhn.pinpoint.rpc.stream.ClientStreamChannelMessageListener;
import com.nhn.pinpoint.rpc.stream.StreamChannelContext;
import com.nhn.pinpoint.rpc.util.AssertUtils;


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

        if (socketHandler.isSupportServerMode()) {
        	socketHandler.turnOnServerMode();
        }
        
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
        
        // Pinpoint 소켓 내부 객체가 되기전에 listener를 먼저 등록        
        if (socketHandler.isSupportServerMode()) {
        	socketHandler.turnOnServerMode();
        }
        
        this.socketHandler = socketHandler;
        
        notifyReconnectEvent();
    }
    
	// reconnectEventListener의 경우 직접 생성자 호출시에 Dummy를 포함하고 있으며, 
    // setter를 통해서도 접근을 못하게 하기 때문에 null이 아닌 것이 보장됨
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
        // 실패를 리턴하는 StreamChannel을 던져야 되는데. StreamChannel을 interface로 변경해야 됨.
        // 일단 그냥 ex를 던지도록 하겠음.
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
     * ping packet을 tcp 채널에 write한다.
     * write 실패시 PinpointSocketException이 throw 된다.
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

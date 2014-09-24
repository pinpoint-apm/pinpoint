package com.nhn.pinpoint.profiler.sender;


import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.thrift.TBase;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.util.HashedWheelTimer;
import org.jboss.netty.util.Timeout;
import org.jboss.netty.util.Timer;
import org.jboss.netty.util.TimerTask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.nhn.pinpoint.rpc.Future;
import com.nhn.pinpoint.rpc.FutureListener;
import com.nhn.pinpoint.rpc.ResponseMessage;
import com.nhn.pinpoint.rpc.client.PinpointSocket;
import com.nhn.pinpoint.rpc.client.PinpointSocketReconnectEventListener;
import com.nhn.pinpoint.rpc.util.TimerFactory;
import com.nhn.pinpoint.thrift.dto.TResult;
import com.nhn.pinpoint.thrift.io.HeaderTBaseDeserializer;
import com.nhn.pinpoint.thrift.io.HeaderTBaseDeserializerFactory;
import com.nhn.pinpoint.thrift.io.HeaderTBaseSerializer;
import com.nhn.pinpoint.thrift.io.HeaderTBaseSerializerFactory;

/**
 * @author emeroad
 * @author koo.taejin
 * @author netspider
 */
public class TcpDataSender extends AbstractDataSender implements EnhancedDataSender {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    static {
        // preClassLoad
        ChannelBuffers.buffer(2);
    }

    private final PinpointSocket socket;
    private final Timer timer;
    
    private final AtomicBoolean fireState = new AtomicBoolean(false);

    private final WriteFailFutureListener writeFailFutureListener;


    private final HeaderTBaseSerializer serializer = HeaderTBaseSerializerFactory.DEFAULT_FACTORY.createSerializer();

    private final RetryQueue retryQueue = new RetryQueue();

    private AsyncQueueingExecutor<Object> executor;

    public TcpDataSender(PinpointSocket socket) {
    	this.socket = socket;
    	this.timer = createTimer();
    	writeFailFutureListener = new WriteFailFutureListener(logger, "io write fail.", "host", -1);
        this.executor = createAsyncQueueingExecutor(1024 * 5, "Pinpoint-TcpDataExecutor");
    }
    
    private Timer createTimer() {
        HashedWheelTimer timer = TimerFactory.createHashedWheelTimer("Pinpoint-DataSender-Timer", 100, TimeUnit.MILLISECONDS, 512);
        timer.start();
        return timer;
    }
    
    @Override
    public boolean send(TBase<?, ?> data) {
        return executor.execute(data);
    }

    @Override
    public boolean request(TBase<?, ?> data) {
        return this.request(data, 3);
    }

    @Override
    public boolean request(TBase<?, ?> data, int retryCount) {
    	RequestMarker message = new RequestMarker(data, retryCount);
        return executor.execute(message);
    }

	@Override
	public boolean request(TBase<?, ?> data, FutureListener<ResponseMessage> listener) {
    	RequestMarker message = new RequestMarker(data, listener);
        return executor.execute(message);
	}

	@Override
	public boolean addReconnectEventListener(PinpointSocketReconnectEventListener eventListener) {
		return this.socket.addPinpointSocketReconnectEventListener(eventListener);
	}

	@Override
	public boolean removeReconnectEventListener(PinpointSocketReconnectEventListener eventListener) {
		return this.socket.removePinpointSocketReconnectEventListener(eventListener);
	}

    @Override
    public void stop() {
        executor.stop();

        Set<Timeout> stop = timer.stop();
        if (!stop.isEmpty()) {
            logger.info("stop Timeout:{}", stop.size());
        }
    }

    @Override
    protected void sendPacket(Object message) {
        try {
        	if (message instanceof TBase) {
        		byte[] copy = serialize(serializer, (TBase) message);
                if (copy == null) {
                    return;
                }
                doSend(copy);
        	} else if (message instanceof RequestMarker) {
        		RequestMarker requestMarker = (RequestMarker) message;

        		TBase tBase = requestMarker.getTBase();
        		int retryCount = requestMarker.getRetryCount();
        		FutureListener futureListener = requestMarker.getFutureListener();
        		byte[] copy = serialize(serializer, tBase);
                if (copy == null) {
                    return;
                }
                
                if (futureListener != null) {
                	doRequest(copy, futureListener);
                } else {
                	doRequest(copy, retryCount, tBase);
                }
        	} else {
                logger.error("sendPacket fail. invalid dto type:{}", message.getClass());
                return;
        	}
        } catch (Exception e) {
            // 일단 exception 계층이 좀 엉터리라 Exception으로 그냥 잡음.
            logger.warn("tcp send fail. Caused:{}", e.getMessage(), e);
        }
    }

    private void doSend(byte[] copy) {
        Future write = this.socket.sendAsync(copy);
        write.setListener(writeFailFutureListener);
    }

    private void doRequest(final byte[] requestPacket, final int retryCount, final Object targetClass) {
    	FutureListener futureListner = (new FutureListener<ResponseMessage>() {
            @Override
            public void onComplete(Future<ResponseMessage> future) {
                if (future.isSuccess()) {
            		// caching해야 될려나?
                	HeaderTBaseDeserializer deserializer = HeaderTBaseDeserializerFactory.DEFAULT_FACTORY.createDeserializer();
                    TBase<?, ?> response = deserialize(deserializer, future.getResult());
                    if (response instanceof TResult) {
                        TResult result = (TResult) response;
                        if (result.isSuccess()) {
                            logger.debug("result success");
                        } else {
                            logger.warn("request fail. clazz:{} Caused:{}", targetClass, result.getMessage());
                            retryRequest(requestPacket, retryCount, targetClass.getClass().getSimpleName());
                        }
                    } else {
                        logger.warn("Invalid ResponseMessage. {}", response);
//                         response가 이상하게 오는 케이스는 재전송 케이스가 아니고 로그를 통한 정확한 원인 분석이 필요한 케이스이다.
//                        null이 떨어질수도 있음.
//                        retryRequest(requestPacket);
                    }
                } else {
                    logger.warn("request fail. clazz:{} Caused:{}", targetClass, future.getCause().getMessage(), future.getCause());
                    retryRequest(requestPacket, retryCount, targetClass.getClass().getSimpleName());
                }
            }
        });
    	
    	doRequest(requestPacket, futureListner);
    }

    private void retryRequest(byte[] requestPacket, int retryCount, final String className) {
        RetryMessage retryMessage = new RetryMessage(retryCount, requestPacket);
        retryQueue.add(retryMessage);
        if (fireTimeout()) {
        	timer.newTimeout(new TimerTask() {
                @Override
                public void run(Timeout timeout) throws Exception {
                    while(true) {
                        RetryMessage retryMessage = retryQueue.get();
                        if (retryMessage == null) {
                            // 동시성이 약간 안맞을 가능성 있는거 같기는 하나. 크게 문제 없을거 같아서 일단 패스.
                            fireComplete();
                            return;
                        }
                        int fail = retryMessage.fail();
                        doRequest(retryMessage.getBytes(), fail, className);
                    }
                }
            }, 1000 * 10, TimeUnit.MILLISECONDS);
        }
    }

    private void doRequest(final byte[] requestPacket, FutureListener futureListener) {
        final Future<ResponseMessage> response = this.socket.request(requestPacket);
        response.setListener(futureListener);
    }

    private boolean fireTimeout() {
        if (fireState.compareAndSet(false, true)) {
            return true;
        } else {
            return false;
        }
    }

    private void fireComplete() {
        logger.debug("fireComplete");
        fireState.compareAndSet(true, false);
    }

	@Override
	public boolean isNetworkAvailable() {
		if (this.socket == null) {
			return false;
		}
		return this.socket.isConnected();
	}
}

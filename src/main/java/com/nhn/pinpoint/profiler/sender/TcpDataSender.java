package com.nhn.pinpoint.profiler.sender;


import com.nhn.pinpoint.thrift.dto.TResult;
import com.nhn.pinpoint.thrift.io.HeaderTBaseDeserializer;
import com.nhn.pinpoint.thrift.io.SafeHeaderTBaseSerializer;
import com.nhn.pinpoint.rpc.Future;
import com.nhn.pinpoint.rpc.FutureListener;
import com.nhn.pinpoint.rpc.PinpointSocketException;
import com.nhn.pinpoint.rpc.ResponseMessage;
import com.nhn.pinpoint.rpc.client.PinpointSocket;
import com.nhn.pinpoint.rpc.client.PinpointSocketFactory;
import org.apache.thrift.TBase;
import org.apache.thrift.TException;
import org.jboss.netty.util.Timeout;
import org.jboss.netty.util.TimerTask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author emeroad
 */
public class TcpDataSender implements EnhancedDataSender {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final PinpointSocketFactory pinpointSocketFactory;
    private PinpointSocket socket;
    private final int connectRetryCount = 3;

    private final AtomicBoolean fireState = new AtomicBoolean(false);

    private final WriteFailFutureListener writeFailFutureListener;


    private final SafeHeaderTBaseSerializer serializer = new SafeHeaderTBaseSerializer();

    private final RetryQueue retryQueue = new RetryQueue();

    private AsyncQueueingExecutor<Object> executor;

    public TcpDataSender(String host, int port) {
        pinpointSocketFactory = new PinpointSocketFactory();
        pinpointSocketFactory.setTimeoutMillis(1000 * 5);
        writeFailFutureListener = new WriteFailFutureListener(logger, "io write fail.", host, port);
        connect(host, port);

        this.executor = getExecutor();
    }

    private void connect(String host, int port) {
        for (int i = 0; i < connectRetryCount; i++) {
            try {
                this.socket = pinpointSocketFactory.connect(host, port);
                logger.info("tcp connect success:{}/{}", host, port);
                return;
            } catch (PinpointSocketException e) {
                logger.warn("tcp connect fail:{}/{} try reconnect, retryCount:{}", host, port, i);
            }
        }
        logger.warn("change background tcp connect mode  {}/{} ", host, port);
        this.socket = pinpointSocketFactory.scheduledConnect(host, port);
    }

    public AsyncQueueingExecutor<Object> getExecutor() {
        final AsyncQueueingExecutor<Object> executor = new AsyncQueueingExecutor<Object>(1024 * 5, "Pinpoint-TcpDataExecutor");
        executor.setListener(new AsyncQueueingExecutorListener<Object>() {
            @Override
            public void execute(Collection<Object> dtoList) {
                sendPacketN(dtoList);
            }

            @Override
            public void execute(Object dto) {
                sendPacket(dto);
            }
        });
        return executor;
    }


    private void sendPacketN(Collection<Object> dtoList) {
        Object[] dataList = dtoList.toArray();
//          일단 single thread에서 하는거라 구지 복사 안해도 될것 같음.
//        Object[] copy = Arrays.copyOf(original, original.length);

//        for (Object data : dataList) {
//        이렇게 바꾸지 말것. copy해서 return 하는게 아니라 항상 max치가 나옴.
            final int size = dtoList.size();
            for (int i = 0; i < size; i++) {
            try {
                sendPacket(dataList[i]);
            } catch (Throwable th) {
                logger.warn("Unexpected Error. Cause:{}", th.getMessage(), th);
            }
        }

    }

    private void sendPacket(Object dto) {
        try {
            if (dto instanceof TBase) {
                TBase<?, ?> tBase = (TBase<?, ?>) dto;
                byte[] copy = serialize(tBase);
                if (copy == null) {
                    return;
                }
                doSend(copy);
            } else if(dto instanceof RequestMarker) {
                RequestMarker requestMarker = (RequestMarker) dto;
                final TBase<?, ?> tBase = requestMarker.getTBase();
                int retry = requestMarker.getRetryCount();
                byte[] copy = serialize(tBase);
                if (copy == null) {
                    return;
                }
                doRequest(copy, retry, tBase);
            } else {
                logger.error("sendPacket fail. invalid dto type:{}", dto.getClass());
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
        // 리팩토링 필요.
        final Future<ResponseMessage> response = this.socket.request(requestPacket);
        response.setListener(new FutureListener<ResponseMessage>() {
            @Override
            public void onComplete(Future<ResponseMessage> future) {
                if (future.isSuccess()) {
                    TBase<?, ?> response = deserialize(future);
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
    }

    private void retryRequest(byte[] requestPacket, int retryCount, final String className) {
        RetryMessage retryMessage = new RetryMessage(retryCount, requestPacket);
        retryQueue.add(retryMessage);
        if (fireTimeout()) {
            pinpointSocketFactory.newTimeout(new TimerTask() {
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

    private TBase<?, ?> deserialize(Future<ResponseMessage> future) {
        byte[] message = future.getResult().getMessage();
        // caching해야 될려나?
        HeaderTBaseDeserializer deserializer = new HeaderTBaseDeserializer();
        try {
            return deserializer.deserialize(message);
        } catch (TException e) {
            if (logger.isWarnEnabled()) {
                logger.warn("Deserialize fail. Caused:{}", e.getMessage(), e);
            }
            return null;
        }
    }

    private byte[] serialize(TBase<?, ?> dto) {
        try {
            return serializer.serialize(dto);
        } catch (TException e) {
            if (logger.isWarnEnabled()) {
                logger.warn("Serialize fail:{} Caused:{}", dto, e.getMessage(), e);
            }
            return null;
        }
    }

    @Override
    public boolean request(TBase<?, ?> data) {
        return this.request(data, 3);
    }

    @Override
    public boolean request(TBase<?, ?> data, int retryCount) {
        RequestMarker requestMarker = new RequestMarker(data, retryCount);
        return executor.execute(requestMarker);
    }

    @Override
    public boolean send(TBase<?, ?> data) {
        return executor.execute(data);
    }

    @Override
    public void stop() {
        executor.stop();
        socket.close();
        pinpointSocketFactory.release();
    }

    private static class RequestMarker {
        private final TBase tBase;
        private final int retryCount;

        private RequestMarker(TBase tBase, int retryCount) {
            this.tBase = tBase;
            this.retryCount = retryCount;
        }

        private TBase getTBase() {
            return tBase;
        }

        private int getRetryCount() {
            return retryCount;
        }
    }

}

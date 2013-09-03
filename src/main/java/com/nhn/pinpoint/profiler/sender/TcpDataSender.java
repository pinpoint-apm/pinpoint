package com.nhn.pinpoint.profiler.sender;


import com.nhn.pinpoint.common.dto.thrift.Result;
import com.nhn.pinpoint.profiler.context.Thriftable;
import com.nhn.pinpoint.profiler.io.HeaderTBaseDeserializer;
import com.nhn.pinpoint.profiler.io.SafeHeaderTBaseSerializer;
import com.nhn.pinpoint.profiler.logging.Logger;
import com.nhn.pinpoint.profiler.logging.LoggerFactory;
import com.nhn.pinpoint.rpc.Future;
import com.nhn.pinpoint.rpc.FutureListener;
import com.nhn.pinpoint.rpc.PinpointSocketException;
import com.nhn.pinpoint.rpc.ResponseMessage;
import com.nhn.pinpoint.rpc.client.PinpointSocket;
import com.nhn.pinpoint.rpc.client.PinpointSocketFactory;
import org.apache.thrift.TBase;
import org.apache.thrift.TException;

import java.util.Collection;

/**
 *
 */
public class TcpDataSender implements DataSender {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());


    private final PinpointSocketFactory pinpointSocketFactory;
    private PinpointSocket socket;
    private final int connectRetryCount = 3;
    private final WriteFailFutureListener writeFailFutureListener;


    private final SafeHeaderTBaseSerializer serializer = new SafeHeaderTBaseSerializer();

    private AsyncQueueingExecutor executor;

    public TcpDataSender(String host, int port) {
        pinpointSocketFactory = new PinpointSocketFactory();
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
                logger.warn("tcp connect fail:{}/{} try reconnect, retryCount:{}", new Object[]{host, port, i});
            }
        }
        logger.warn("change background tcp connect mode  {}/{} ", host, port);
        this.socket = pinpointSocketFactory.scheduledConnect(host, port);
    }

    public AsyncQueueingExecutor getExecutor() {
        AsyncQueueingExecutor executor = new AsyncQueueingExecutor(1024 * 5, "Pinpoint-TcpDataExecutor");
        executor.setListener(new AsyncQueueingExecutorListener() {
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
        TBase<?, ?> tBase;
        boolean request = false;
        if (dto instanceof TBase) {
            tBase = (TBase<?, ?>) dto;
        } else if (dto instanceof Thriftable) {
            tBase = ((Thriftable) dto).toThrift();
        } else if(dto instanceof RequestMarker) {
            tBase = ((RequestMarker) dto).getTBase();
            request = true;
        } else {
            logger.warn("sendPacket fail. invalid dto type:{}", dto.getClass());
            return;
        }
        byte[] copy = serialize(tBase);
        if (copy == null) {
            return;
        }


        // 일단 send로 함. 추가로 request and response로 교체나 추가 api로 교체하고 재전송 로직을 어느정도 확보할것
        try {
            if (request) {
                Future<ResponseMessage> response = this.socket.request(copy);
                response.setListener(new FutureListener<ResponseMessage>() {
                    @Override
                    public void onComplete(Future<ResponseMessage> future) {
                        if (future.isSuccess()) {
                            TBase<?, ?> response = deserialize(future);
                            if (response instanceof Result) {
                                Result result = (Result) response;
                                if (result.isSuccess()) {
                                    logger.debug("result ok");
                                } else {
                                    logger.warn("request fail. Caused:{}", result.getMessage());
                                }
                            } else {
                                logger.warn("Invalid ResponseMessage. {}", response);
                            }
                        } else {
                            logger.warn("request send fail");
                        }
                    }
                });
            } else  {
                Future ioWriteCheck = this.socket.sendAsync(copy);
                ioWriteCheck.setListener(writeFailFutureListener);
            }
        } catch (Exception e) {
            // 일단 exception 계층이 좀 엉터리라 Exception으로 그냥 잡음.
            logger.warn("tcp send fail. Caused:{}", e.getMessage(), e);
        }
    }

    private TBase<?, ?> deserialize(Future<ResponseMessage> future) {
        byte[] message = future.getResult().getMessage();
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
                logger.warn("Serialize fail:{} Caused:{}", new Object[]{dto, e.getMessage(), e});
            }
            return null;
        }
    }

    @Override
    public boolean request(TBase<?, ?> data) {
        RequestMarker requestMarker = new RequestMarker(data);
        return executor.execute(requestMarker);
    }

    @Override
    public boolean send(TBase<?, ?> data) {
        return executor.execute(data);
    }

    @Override
    public boolean send(Thriftable thriftable) {
        return executor.execute(thriftable);
    }

    @Override
    public void stop() {
        executor.stop();
        socket.close();
        pinpointSocketFactory.release();
    }

    private static class RequestMarker {
        private TBase tBase;

        private RequestMarker(TBase tBase) {
            this.tBase = tBase;
        }

        private TBase getTBase() {
            return tBase;
        }
    }


}

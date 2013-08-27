package com.nhn.pinpoint.rpc.client;

import com.nhn.pinpoint.common.util.PinpointThreadFactory;
import com.nhn.pinpoint.rpc.DefaultFuture;
import com.nhn.pinpoint.rpc.FailureEventHandler;
import com.nhn.pinpoint.rpc.PinpointSocketException;
import com.nhn.pinpoint.rpc.ResponseMessage;
import com.nhn.pinpoint.rpc.packet.RequestPacket;
import com.nhn.pinpoint.rpc.packet.ResponsePacket;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.util.HashedWheelTimer;
import org.jboss.netty.util.ThreadNameDeterminer;
import org.jboss.netty.util.Timeout;
import org.jboss.netty.util.Timer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 *
 */
public class RequestManager {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private static final ThreadFactory THREAD_FACTORY = new PinpointThreadFactory("Pinpoint-RequestManager-Timer", true);

    private final AtomicInteger requestId = new AtomicInteger(1);

    private final ConcurrentMap<Integer, DefaultFuture<ResponseMessage>> requestMap = new ConcurrentHashMap<Integer, DefaultFuture<ResponseMessage>>();
    // Timer를 factory로 옮겨야 되나?
    private final Timer timer;

    public RequestManager() {
        this(100);
    }

    public RequestManager(long timeoutTickDuration) {
        timer = new HashedWheelTimer(THREAD_FACTORY, ThreadNameDeterminer.CURRENT, timeoutTickDuration, TimeUnit.MILLISECONDS, 512);
        // 구지 start를 안함. 어차피 newTimeout호출하면 start체크하니. reqeust response있을때 자동으로 시작될거라 그게 더 나은듯.
    }



    private FailureEventHandler createFailureEventHandler(final int requestId) {
        FailureEventHandler failureEventHandler = new FailureEventHandler() {
            @Override
            public boolean fireFailure() {
                DefaultFuture<ResponseMessage> future = removeMessageFuture(requestId);
                if (future != null) {
                    // 정확하게 지워짐.
                    return true;
                }
                return false;
            }
        };
        return failureEventHandler;
    }


    private void addTimeoutTask(long timeoutMillis, DefaultFuture future) {
        if (future == null) {
            throw new NullPointerException("future");
        }
        try {
            Timeout timeout = timer.newTimeout(future, timeoutMillis, TimeUnit.MILLISECONDS);
            future.setTimeout(timeout);
        } catch (IllegalStateException e) {
            // timer가 shutdown되었을 경우인데. 이것은 socket이 closed되었다는 의미뿐이 없을거임..
            future.setFailure(new PinpointSocketException("socket closed")) ;
        }
    }

    private int getNextRequestId() {
        return this.requestId.getAndIncrement();
    }


    public void messageReceived(ResponsePacket responsePacket, Channel channel) {
        final int requestId = responsePacket.getRequestId();
        final DefaultFuture<ResponseMessage> future = removeMessageFuture(requestId);
        if (future == null) {
            logger.warn("future not found:{}, channel:{}", responsePacket, channel);
            return;
        } else {
            logger.debug("responsePacket arrived packet:{}, channel:{}", responsePacket, channel);
        }

        ResponseMessage response = new ResponseMessage();
        response.setMessage(responsePacket.getPayload());
        future.setResult(response);
    }

    public DefaultFuture<ResponseMessage> removeMessageFuture(int requestId) {
        return this.requestMap.remove(requestId);
    }

    public void messageReceived(RequestPacket requestPacket, Channel channel) {
        logger.error("unexpectedMessage received:{} address:{}", requestPacket, channel.getRemoteAddress());
    }



    public DefaultFuture<ResponseMessage> register(RequestPacket requestPacket, long timeoutMillis) {
        // shutdown check
        final int requestId = getNextRequestId();
        requestPacket.setRequestId(requestId);

        final DefaultFuture<ResponseMessage> future = new DefaultFuture<ResponseMessage>(timeoutMillis);

        final DefaultFuture old = this.requestMap.put(requestId, future);
        if (old != null) {
            throw new PinpointSocketException("unexpected error. old future exist:" + old + " id:" + requestId);
        }
        // future가 실패하였을 경우 requestMap에서 빠르게 지울수 있도록 핸들을 넣는다.
        FailureEventHandler removeTable = createFailureEventHandler(requestId);
        future.setFailureEventHandler(removeTable);

        addTimeoutTask(timeoutMillis, future);
        return future;
    }


    public void close() {
        final PinpointSocketException closed = new PinpointSocketException("socket closed");

        // close의 동시성 타이밍을 좀더 좋게 맞출수는 없나?
        final Timer timer = this.timer;
        if (timer != null) {
            Set<Timeout> stop = timer.stop();
            for (Timeout timeout : stop) {
                DefaultFuture future = (DefaultFuture)timeout.getTask();
                future.setFailure(closed);
            }
        }

        for (Map.Entry<Integer, DefaultFuture<ResponseMessage>> entry : requestMap.entrySet()) {
            entry.getValue().setFailure(closed);
        }
        this.requestMap.clear();


    }

}


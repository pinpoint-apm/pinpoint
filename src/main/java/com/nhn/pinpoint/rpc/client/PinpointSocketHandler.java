package com.nhn.pinpoint.rpc.client;

import com.nhn.pinpoint.rpc.*;
import com.nhn.pinpoint.rpc.packet.*;
import com.nhn.pinpoint.rpc.util.TimerFactory;
import org.jboss.netty.channel.*;
import org.jboss.netty.util.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.SocketAddress;
import java.util.concurrent.TimeUnit;

/**
 * @author emeroad
 */
public class PinpointSocketHandler extends SimpleChannelHandler implements SocketHandler {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final State state = new State();

    private volatile Channel channel;

    private long timeoutMillis = 3 * 1000;
    private long pingDelay = 60 * 1000 * 5;

    private final Timer channelTimer;

    private final PinpointSocketFactory pinpointSocketFactory;
    private SocketAddress connectSocketAddress;
    private volatile PinpointSocket pinpointSocket;

    private final RequestManager requestManager;
    private final StreamChannelManager streamChannelManager;

    private final ChannelFutureListener pingWriteFailFutureListener = new WriteFailFutureListener(this.logger, "ping write fail.", "ping write success.");
    private final ChannelFutureListener sendWriteFailFutureListener = new WriteFailFutureListener(this.logger, "send() write fail.", "send() write fail.");


    public PinpointSocketHandler(PinpointSocketFactory pinpointSocketFactory, long pingDelay, long timeoutMillis) {
        if (pinpointSocketFactory == null) {
            throw new NullPointerException("pinpointSocketFactory must not be null");
        }
        HashedWheelTimer timer = TimerFactory.createHashedWheelTimer("Pinpoint-SocketHandler-Timer", 100, TimeUnit.MILLISECONDS, 512);
        timer.start();
        this.channelTimer = timer;
        this.pinpointSocketFactory = pinpointSocketFactory;
        this.requestManager = new RequestManager(timer);
        this.streamChannelManager = new StreamChannelManager();
        this.pingDelay = pingDelay;
        this.timeoutMillis = timeoutMillis;
    }

    public Timer getChannelTimer() {
        return channelTimer;
    }

    public void setPinpointSocket(PinpointSocket pinpointSocket) {
        if (pinpointSocket == null) {
            throw new NullPointerException("pinpointSocket must not be null");
        }
        
        this.pinpointSocket = pinpointSocket;
    }


    public void setConnectSocketAddress(SocketAddress connectSocketAddress) {
        if (connectSocketAddress == null) {
            throw new NullPointerException("connectSocketAddress must not be null");
        }
        this.connectSocketAddress = connectSocketAddress;
    }

    @Override
    public void channelOpen(ChannelHandlerContext ctx, ChannelStateEvent e) throws Exception {
        Channel channel = e.getChannel();
        if (logger.isDebugEnabled()) {
            logger.debug("channelOpen() state:{} {}", state.getString(), channel);
        }
        this.channel = channel;

    }

    public void open() {
        logger.info("open() change state=RUN");
        if (!state.changeRun()) {
            throw new IllegalStateException("invalid open state:" + state.getString());
        }

    }

    @Override
    public void initReconnect() {
        logger.info("initReconnect() change state=INIT_RECONNECT");
        state.setState(State.INIT_RECONNECT);
    }

    @Override
    public void channelConnected(ChannelHandlerContext ctx, ChannelStateEvent e) throws Exception {
        if (logger.isDebugEnabled()) {
            logger.debug("channelConnected() state:{} {}", state.getString(), channel);
        }
        registerPing();
    }

    private void registerPing() {
        final PingTask pingTask = new PingTask();
        newPingTimeout(pingTask);
    }

    private void newPingTimeout(TimerTask pingTask) {
        this.channelTimer.newTimeout(pingTask, pingDelay, TimeUnit.MILLISECONDS);
    }

    private class PingTask implements TimerTask {
        @Override
        public void run(Timeout timeout) throws Exception {
            if (timeout.isCancelled()) {
                newPingTimeout(this);
                return;
            }
            if (isClosed()) {
                return;
            }
            writePing();
            newPingTimeout(this);
        }
    }

    void writePing() {
        if (!isRun()) {
            return;
        }
        logger.debug("writePing {}", channel);
        ChannelFuture write = this.channel.write(PingPacket.PING_PACKET);
        write.addListener(pingWriteFailFutureListener);
    }

    public void sendPing() {
        if (!isRun()) {
            return;
        }
        logger.debug("sendPing {}", channel);
        ChannelFuture write = this.channel.write(PingPacket.PING_PACKET);
        write.awaitUninterruptibly();
        if (!write.isSuccess()) {
            Throwable cause = write.getCause();
            throw new PinpointSocketException("send ping fail. Caused:" + cause.getMessage(), cause);
        }
        logger.debug("sendPing success {}", channel);
    }



    public void send(byte[] bytes) {
        if (bytes == null) {
            throw new NullPointerException("bytes");
        }
        ChannelFuture future = send0(bytes);
        future.addListener(sendWriteFailFutureListener);
    }

    public Future sendAsync(byte[] bytes) {
        if (bytes == null) {
            throw new NullPointerException("bytes");
        }

        ChannelFuture channelFuture = send0(bytes);
        final ChannelWriteCompleteListenableFuture future = new ChannelWriteCompleteListenableFuture(timeoutMillis);
        channelFuture.addListener(future);
        return future ;
    }

    public void sendSync(byte[] bytes) {
        if (bytes == null) {
            throw new NullPointerException("bytes");
        }
        ChannelFuture write = send0(bytes);
        await(write);
    }

    private void await(ChannelFuture channelFuture) {
        try {
            channelFuture.await(timeoutMillis, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        if (channelFuture.isDone()) {
            boolean success = channelFuture.isSuccess();
            if (success) {
                return;
            } else {
                final Throwable cause = channelFuture.getCause();
                throw new PinpointSocketException(cause);
            }
        } else {
            boolean cancel = channelFuture.cancel();
            if (cancel) {
                // 3초에도 io가 안끝나면 일단 timeout인가?
                throw new PinpointSocketException("io timeout");
            } else {
                // 성공했으니. 위와 로직이 동일할듯.
                boolean success = channelFuture.isSuccess();
                if (success) {
                    return;
                } else {
                    final Throwable cause = channelFuture.getCause();
                    throw new PinpointSocketException(cause);
                }
            }
        }
    }

    private ChannelFuture send0(byte[] bytes) {
        ensureOpen();
        SendPacket send = new SendPacket(bytes);

        return this.channel.write(send);
    }

    public Future<ResponseMessage> request(byte[] bytes) {
        if (bytes == null) {
            throw new NullPointerException("bytes");
        }

        boolean run = isRun();
        if (!run) {
            DefaultFuture<ResponseMessage> closedException = new DefaultFuture<ResponseMessage>();
            closedException.setFailure(new PinpointSocketException("invalid state:" + state.getString() + " channel:" + channel));
            return closedException;
        }

        RequestPacket request = new RequestPacket(bytes);

        final Channel channel = this.channel;
        final ChannelWriteFailListenableFuture<ResponseMessage> messageFuture = this.requestManager.register(request, this.timeoutMillis);

        ChannelFuture write = channel.write(request);
        write.addListener(messageFuture);

        return messageFuture;
    }


    public StreamChannel createStreamChannel() {
        ensureOpen();

        final Channel channel = this.channel;
        return this.streamChannelManager.createStreamChannel(channel);
    }


    @Override
    public void messageReceived(ChannelHandlerContext ctx, MessageEvent e) throws Exception {
        final Object message = e.getMessage();
        if (message instanceof Packet) {
            final Packet packet = (Packet) message;
            final short packetType = packet.getPacketType();
            switch (packetType) {
                case PacketType.APPLICATION_RESPONSE:
                    this.requestManager.messageReceived((ResponsePacket) message, e.getChannel());
                    return;
                case PacketType.APPLICATION_REQUEST:
                    this.requestManager.messageReceived((RequestPacket) message, e.getChannel());
                    return;
                // connector로 들어오는 request 메시지를 핸들링을 해야 함.
                case PacketType.APPLICATION_STREAM_CREATE:
                case PacketType.APPLICATION_STREAM_CLOSE:
                case PacketType.APPLICATION_STREAM_CREATE_SUCCESS:
                case PacketType.APPLICATION_STREAM_CREATE_FAIL:
                case PacketType.APPLICATION_STREAM_RESPONSE:
                    this.streamChannelManager.messageReceived((StreamPacket) message, e.getChannel());
                    return;
                case PacketType.CONTROL_SERVER_CLOSE:
                    messageReceivedServerClosed(e.getChannel());
                    return;
                default:
                    logger.warn("unexpectedMessage received:{} address:{}", message, e.getRemoteAddress());
            }
        } else {
            logger.warn("invalid messageReceived:{}", message);
        }
    }

    private void messageReceivedServerClosed(Channel channel) {
        logger.info("ServerClosed Packet received. {}", channel);
        // reconnect 상태로 변경한다.
        state.setState(State.RECONNECT);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e) throws Exception {
        Throwable cause = e.getCause();
        if (state.getState() == State.INIT_RECONNECT) {
            // 재접속시 stackTrace는 제거하였음. 로그가 너무 많이 나옴.
            logger.info("exceptionCaught() reconnect fail. state:{} {} Caused:{}", state.getString(), e.getChannel(), cause.getMessage());
        } else {
            logger.warn("exceptionCaught() UnexpectedError happened. state:{} {} Caused:{}", state.getString(), e.getChannel(), cause.getMessage(), cause);
        }
        // error가 발생하였을 경우의 동작을 더 정확히 해야 될듯함.
//          아래처럼 하면 상대방이 그냥 죽었을때 reconnet가 안됨.
//        state.setClosed();
//        Channel channel = e.getChannel();
//        if (channel.isConnected()) {
//            channel.close();
//        }

    }

    @Override
    public void channelClosed(final ChannelHandlerContext ctx, final ChannelStateEvent e) throws Exception {
        final int currentState = state.getState();
        if (currentState == State.CLOSED) {
            logger.debug("channelClosed() normal. state:{} {}", state.getString(currentState), e.getChannel());
            return;
        } else if(currentState == State.INIT_RECONNECT){
            logger.debug("channelClosed() reconnect fail. state:{} {}", state.getString(currentState), e.getChannel());
        } else if (currentState == State.RUN || currentState == State.RECONNECT) {
            // 여기서 부터 비정상 closed라고 볼수 있다.
            if (currentState == State.RUN) {
                logger.debug("change state=reconnect");
                state.setState(State.RECONNECT);
            }
            logger.info("channelClosed() UnexpectedChannelClosed. state:{} try reconnect channel:{}, connectSocketAddress:{}", state.getString(), e.getChannel(), connectSocketAddress);

            this.pinpointSocketFactory.reconnect(this.pinpointSocket, this.connectSocketAddress);
            return;
        } else {
            logger.info("channelClosed() UnexpectedChannelClosed. state:{} {}", state.getString(currentState), e.getChannel());
        }
        releaseResource();
    }



    private void ensureOpen() {
        final int currentState = state.getState();
        if (currentState == State.RUN) {
            return;
        }
        if (currentState == State.CLOSED) {
            throw new PinpointSocketException("already closed");
        } else if(currentState == State.RECONNECT) {
            throw new PinpointSocketException("reconnecting...");
        }
        logger.info("invalid socket state:{}", state.getString(currentState));
        throw new PinpointSocketException("invalid socket state:" + currentState);
    }


    boolean isRun() {
        return state.isRun();
    }

    boolean isClosed() {
        return state.isClosed();
    }

    public void close() {
        logger.debug("close() call");
        int currentState = this.state.getState();
        if (currentState == State.CLOSED) {
            logger.debug("already close()");
            return;
        }
        logger.debug("close() start");
        if (!this.state.changeClosed(currentState)) {
            logger.info("close() invalid state");
            return;
        }
        logger.debug("close() state change complete");
        // hand shake close
        final Channel channel = this.channel;
        // close packet을 먼저 날리고 resource를 정리해야 되나?
        // resource 정리시 request response 메시지에 대한 에러 처리나, stream 채널의 정리가 필요하니 반대로 해야 되나??  이게 맞는거 같긴한데. timer가 헤깔리네.
        // 헤깔리니. 일단 만들고 추후 수정.
        sendClosedPacket(channel);
        releaseResource();
        logger.debug("channel.close()");

        ChannelFuture channelFuture = channel.close();
        channelFuture.addListener(new WriteFailFutureListener(logger, "close() event fail.", "close() event success."));
        channelFuture.awaitUninterruptibly();
        logger.debug("close() complete");
    }

    private void releaseResource() {
        logger.debug("releaseResource()");
        this.requestManager.close();
        this.streamChannelManager.close();
        this.channelTimer.stop();
    }

    private void sendClosedPacket(Channel channel) {
        if (!channel.isConnected()) {
            logger.debug("channel already closed. skip sendClosedPacket() {}", channel);
            return;
        }
       logger.debug("write ClientClosePacket");
        ClientClosePacket clientClosePacket = new ClientClosePacket();
        ChannelFuture write = channel.write(clientClosePacket);
        write.addListener(new ChannelFutureListener() {
            @Override
            public void operationComplete(ChannelFuture future) throws Exception {
                if (!future.isSuccess()) {
                    logger.warn("ClientClosePacket write fail. channel:{}", future.getCause(), future.getCause());
                } else {
                    logger.debug("ClientClosePacket write success. channel:{}", future.getChannel());
                }
            }
        });
        write.awaitUninterruptibly(3000, TimeUnit.MILLISECONDS);
    }


    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("PinpointSocketHandler{");
        sb.append("channel=").append(channel);
        sb.append('}');
        return sb.toString();
    }
}

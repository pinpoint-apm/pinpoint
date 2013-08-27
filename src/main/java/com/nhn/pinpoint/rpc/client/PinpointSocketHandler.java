package com.nhn.pinpoint.rpc.client;

import com.nhn.pinpoint.common.util.PinpointThreadFactory;
import com.nhn.pinpoint.rpc.DefaultFuture;
import com.nhn.pinpoint.rpc.Future;
import com.nhn.pinpoint.rpc.PinpointSocketException;
import com.nhn.pinpoint.rpc.ResponseMessage;
import com.nhn.pinpoint.rpc.packet.*;
import org.jboss.netty.channel.*;
import org.jboss.netty.util.HashedWheelTimer;
import org.jboss.netty.util.ThreadNameDeterminer;
import org.jboss.netty.util.Timer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.SocketAddress;
import java.util.concurrent.TimeUnit;

/**
 *
 */
public class PinpointSocketHandler extends SimpleChannelHandler implements SocketHandler {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final State state = new State();

    private volatile Channel channel;

    private long timeoutMillis = 3000;

    private final Timer timer;

    private final PinpointSocketFactory pinpointSocketFactory;
    private SocketAddress connectSocketAddress;
    private volatile PinpointSocket pinpointSocket;

    private final RequestManager requestManager;
    private final StreamChannelManager streamChannelManager;

    private final ChannelFutureListener pingWriteFailFutureListener = new WriteFailFutureListener(this.logger, "ping write fail.");


    public PinpointSocketHandler(PinpointSocketFactory pinpointSocketFactory) {
        if (pinpointSocketFactory == null) {
            throw new NullPointerException("pinpointSocketFactory must not be null");
        }
        HashedWheelTimer timer = new HashedWheelTimer(new PinpointThreadFactory("Pinpoint-SocketHandler-Timer", true), ThreadNameDeterminer.CURRENT, 100, TimeUnit.MILLISECONDS, 512);
        timer.start();
        this.timer = timer;
        this.pinpointSocketFactory = pinpointSocketFactory;
        this.requestManager = new RequestManager(timer);
        this.streamChannelManager = new StreamChannelManager();
    }

    public Timer getTimer() {
        return timer;
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
            logger.debug("channelOpen() {}", channel);
        }
        this.channel = channel;

    }

    public void open() {
        logger.info("change state=run");
        if (!state.changeRun()) {
            throw new IllegalStateException("invalid open state:" + state.getString());
        }

    }

    @Override
    public void channelConnected(ChannelHandlerContext ctx, ChannelStateEvent e) throws Exception {
        if (logger.isDebugEnabled()) {
            logger.debug("channelConnected() {}", channel);
        }
    }

    public void sendPing() {
        if (!this.state.isRun()) {
            return;
        }
        logger.debug("sendPing {}", channel);
        ChannelFuture write = this.channel.write(PingPacket.PING_PACKET);
        write.addListener(pingWriteFailFutureListener);
    }

    public void send(byte[] bytes) {
        if (bytes == null) {
            throw new NullPointerException("bytes");
        }
        send0(bytes);
    }

    public Future sendAsync(byte[] bytes) {
        if (bytes == null) {
            throw new NullPointerException("bytes");
        }
        final DefaultFuture future = new DefaultFuture(timeoutMillis);
        ChannelFuture channelFuture = send0(bytes);
        channelFuture.addListener(new ChannelFutureListener() {
            @Override
            public void operationComplete(ChannelFuture channelfuture) throws Exception {
                boolean success = channelfuture.isSuccess();
                if(success) {
                    future.setResult(null);
                } else {
                    future.setFailure(channelfuture.getCause());
                }
            }
        });
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
            channelFuture.await(3000, TimeUnit.MILLISECONDS);
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
                channelFuture.isSuccess();
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
        final DefaultFuture<ResponseMessage> messageFuture = this.requestManager.register(request, this.timeoutMillis);

        ChannelFuture write = channel.write(request);
        write.addListener(new ChannelFutureListener() {
            @Override
            public void operationComplete(ChannelFuture future) throws Exception {
                if (!future.isSuccess()) {
                    Throwable cause = future.getCause();
                    // io write fail
                    messageFuture.setFailure(cause);
                }
            }
        });

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
                default:
                    logger.warn("unexpectedMessage received:{} address:{}", message, e.getRemoteAddress());
            }
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e) throws Exception {
        Throwable cause = e.getCause();
        logger.warn("exceptionCaught() UnexpectedError happened. state:{} Caused:{}", state.getString(), cause.getMessage(), cause);
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
            logger.debug("channelClosed() state:{} {}", state.getString(currentState), ctx.getChannel());
            return;
        }
        // 여기서 부터 비정상 closed라고 볼수 있다.
        releaseResource();
        if (currentState == State.RUN || currentState == State.RECONNECT) {
            if (currentState == State.RUN) {
                state.setState(State.RECONNECT);
            }
            logger.info("UnexpectedChannelClosed. try reconnect channel:{}, {}, state:{}", e.getChannel(), connectSocketAddress, state.getString());

            this.pinpointSocketFactory.reconnect(this.pinpointSocket, this.connectSocketAddress);
            return;
        }

        logger.info("channelClosed() UnexpectedChannelClosed. channel:{}", e.getChannel());
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
        logger.info("invalid socket state:" + currentState);
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
            logger.debug("close() invalid state");
            return;
        }
        logger.debug("close() state change complete");
        // hand shake close
        final Channel channel = this.channel;
        sendClosedPacket(channel);

        releaseResource();

        channel.close();
        logger.debug("close() complete");
    }

    private void releaseResource() {
        logger.debug("releaseResource()");
        this.requestManager.close();
        this.streamChannelManager.close();
        this.timer.stop();
    }

    private void sendClosedPacket(Channel channel) {
        ClosePacket closePacket = new ClosePacket();
        ChannelFuture write = channel.write(closePacket);
        // write패킷이 io에 써질때까지는 대기를 해야 함.
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

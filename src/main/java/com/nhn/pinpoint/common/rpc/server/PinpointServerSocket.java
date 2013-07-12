package com.nhn.pinpoint.common.rpc.server;

import com.nhn.pinpoint.common.rpc.PinpointSocketException;
import com.nhn.pinpoint.common.rpc.packet.*;
import com.nhn.pinpoint.common.rpc.util.CpuUtils;
import org.jboss.netty.bootstrap.ServerBootstrap;
import org.jboss.netty.channel.*;
import org.jboss.netty.channel.socket.nio.NioServerSocketChannelFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 *
 */
public class PinpointServerSocket extends SimpleChannelHandler {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());
//    private final boolean isDebug = logger.isDebugEnabled();

    private static final int WORKER_COUNT = CpuUtils.workerCount();

    private volatile boolean released;
    private ServerBootstrap bootstrap;

    private Channel serverChannel;

    private ServerMessageListener messageListener = new SimpleLoggingSeverMessageListener();


    public PinpointServerSocket() {
        ServerBootstrap bootstrap = createBootStrap(1, WORKER_COUNT);
        setOptions(bootstrap);
        addPipeline(bootstrap);
        this.bootstrap = bootstrap;
    }

    private void addPipeline(ServerBootstrap bootstrap) {
        ServerPipelineFactory serverPipelineFactory = new ServerPipelineFactory(this);
        bootstrap.setPipelineFactory(serverPipelineFactory);
    }

    void setPipelineFactory(ChannelPipelineFactory channelPipelineFactory) {
        if (channelPipelineFactory == null) {
            throw new NullPointerException("channelPipelineFactory");
        }
        bootstrap.setPipelineFactory(channelPipelineFactory);
    }

    public void setMessageListener(ServerMessageListener messageListener) {
        if (messageListener == null) {
            throw new NullPointerException("messageListener");
        }
        this.messageListener = messageListener;
    }

    private void setOptions(ServerBootstrap bootstrap) {
        // read write timeout이 있어야 되나? nio라서 없어도 되던가?

        // tcp 세팅
        bootstrap.setOption("child.tcpNoDelay", true);
        bootstrap.setOption("child.keepAlive", true);
        // buffer
        bootstrap.setOption("child.sendBufferSize", 4096 * 2);
        bootstrap.setOption("child.receiveBufferSize", 4096 * 2);
    }


    private ServerBootstrap createBootStrap(int bossCount, int workerCount) {
        // profiler, collector,
        ExecutorService boss = Executors.newFixedThreadPool(bossCount);
        ExecutorService worker = Executors.newFixedThreadPool(workerCount);
        NioServerSocketChannelFactory nioClientSocketChannelFactory = new NioServerSocketChannelFactory(boss, worker);
        return new ServerBootstrap(nioClientSocketChannelFactory);
    }

    @Override
    public void messageReceived(ChannelHandlerContext ctx, MessageEvent e) throws Exception {
        Object message = e.getMessage();
        if (message instanceof Packet) {
            final Packet packet = (Packet) message;
            final short packetType = packet.getPacketType();
            final Channel channel = e.getChannel();
            logger.debug("messageReceived:{} channel:{}", message, channel);
            switch (packetType) {
                case PacketType.APPLICATION_SEND: {
                    SocketChannel socketChannel = getChannelContext(channel).getSocketChannel();
                    messageListener.handleSend((SendPacket) message, socketChannel);
                    return;
                }
                case PacketType.APPLICATION_REQUEST: {
                    SocketChannel socketChannel = getChannelContext(channel).getSocketChannel();
                    messageListener.handleRequest((RequestPacket) message, socketChannel);
                    return;
                }
                case PacketType.APPLICATION_STREAM_CREATE:
                case PacketType.APPLICATION_STREAM_CLOSE:
                case PacketType.APPLICATION_STREAM_CREATE_SUCCESS:
                case PacketType.APPLICATION_STREAM_CREATE_FAIL:
                case PacketType.APPLICATION_STREAM_RESPONSE:
                    handleStreamPacket((StreamPacket) message, channel);
                    return;
                default:
                    logger.warn("invalid messageReceived msg:{}, connection:{}", message, e.getChannel());
            }
        } else {
            logger.warn("invalid messageReceived msg:{}, connection:{}", message, e.getChannel());
        }
    }

    private void handleStreamPacket(StreamPacket packet, Channel channel) {
        ChannelContext context = getChannelContext(channel);
        if (packet instanceof StreamCreatePacket) {
            logger.debug("StreamCreate {}, streamId:{}", channel, packet.getChannelId());
            try {
                ServerStreamChannel streamChannel = context.createStreamChannel(packet.getChannelId());
                boolean success = streamChannel.receiveChannelCreate((StreamCreatePacket) packet);
                if (success) {
                    messageListener.handleStream(packet, streamChannel);
                }

            } catch (PinpointSocketException e) {
                logger.warn("channel create fail. channel:{} Caused:{}", channel, e);
            }
        } else if (packet instanceof StreamClosePacket) {
            logger.debug("StreamDestroy {}, streamId:{}", channel, packet.getChannelId());
            ServerStreamChannel streamChannel = context.getStreamChannel(packet.getChannelId());
            boolean close = streamChannel.close();
            if (close) {
                messageListener.handleStream(packet, streamChannel);
            } else {
                logger.warn("invalid streamClosePacket. already close. channel:{} Caused:{}", channel);
            }
        } else {
            logger.warn("invalid streamPacket. channel:{}", channel);
        }
    }


    @Override
    public void channelConnected(ChannelHandlerContext ctx, ChannelStateEvent e) throws Exception {
        final Channel channel = e.getChannel();
        if (logger.isDebugEnabled()) {
            logger.debug("channelConnected {}", channel);
        }
        prepareChannel(channel);
        super.channelConnected(ctx, e);
    }

    @Override
    public void channelClosed(ChannelHandlerContext ctx, ChannelStateEvent e) throws Exception {
        final Channel channel = e.getChannel();
        if (logger.isDebugEnabled()) {
            logger.debug("channelClosed {}", channel);
        }

        final ChannelContext channelContext = getChannelContext(channel);
        channelContext.closeAllStreamChannel();
    }

    private ChannelContext getChannelContext(Channel channel) {
        return (ChannelContext) channel.getAttachment();
    }

    private void prepareChannel(Channel channel) {
        ChannelContext channelContext = new ChannelContext(channel);

        channel.setAttachment(channelContext);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e) throws Exception {
        logger.error("Unexpected Exception happened. event:{}", e, e.getCause());
        Channel channel = e.getChannel();
        channel.close();
    }


    public void bind(String host, int port) throws PinpointSocketException {
        if (released) {
            return;
        }

        InetSocketAddress address = new InetSocketAddress(host, port);
        this.serverChannel = bootstrap.bind(address);
    }


    public void close() {
        synchronized (this) {
            if (released) {
                return;
            }
            released = true;
        }


        if (serverChannel != null) {
            ChannelFuture close = serverChannel.close();
            close.awaitUninterruptibly();
            serverChannel = null;
        }
        if (bootstrap != null) {
            bootstrap.releaseExternalResources();
        }
    }
}

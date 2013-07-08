package com.nhn.pinpoint.common.io.rpc;

import com.nhn.pinpoint.common.io.rpc.packet.*;
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

    private static final int WORKER_COUNT = Runtime.getRuntime().availableProcessors()*2;

    private volatile boolean released;
    private ServerBootstrap bootstrap;

    private Channel serverChannel;

    private ServerMessageListener listener = new SimpleSeverMessageListener();


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

    public void setPipelineFactory(ChannelPipelineFactory channelPipelineFactory) {
        if (channelPipelineFactory == null) {
            throw new NullPointerException("channelPipelineFactory");
        }
        bootstrap.setPipelineFactory(channelPipelineFactory);
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
            switch (packetType) {
                case PacketType.APPLICATION_SEND:
                    listener.handleSend((SendPacket) message, e.getChannel());
                    return;
                case PacketType.APPLICATION_REQUEST:
                    listener.handleRequest((RequestPacket) message, e.getChannel());
                    return;
                case PacketType.APPLICATION_STREAM_CREATE:
                case PacketType.APPLICATION_STREAM_CLOSE:
                case PacketType.APPLICATION_STREAM_CREATE_SUCCESS:
                case PacketType.APPLICATION_STREAM_CREATE_FAIL:
                case PacketType.APPLICATION_STREAM_RESPONSE:
                    listener.handleStream((StreamPacket) message, e.getChannel());
                    return;
                default:
                    logger.error("invalid messageReceived msg:{}, connection:{}", message, e.getChannel());
            }
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e) throws Exception {
        logger.error("Unexpected Exception happened. event:{}", e, e.getCause());
    }


    public void bind(String host, int port) throws PinpointSocketException {
        if (released) {
            return;
        }

        InetSocketAddress address = new InetSocketAddress(host, port);
        this.serverChannel = bootstrap.bind(address);
    }



    public void release() {
        synchronized (this){
            if (released) {
                return;
            }
            released = true;
        }


        if (serverChannel !=null) {
            ChannelFuture close = serverChannel.close();
            close.awaitUninterruptibly();
            serverChannel = null;
        }
        if (bootstrap != null) {
            bootstrap.releaseExternalResources();
        }
    }
}

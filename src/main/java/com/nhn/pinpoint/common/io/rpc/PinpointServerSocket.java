package com.nhn.pinpoint.common.io.rpc;

import org.jboss.netty.bootstrap.ServerBootstrap;
import org.jboss.netty.channel.*;
import org.jboss.netty.channel.socket.nio.NioServerSocketChannelFactory;

import java.net.InetSocketAddress;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 *
 */
public class PinpointServerSocket {


    private static final int WORKER_COUNT = Runtime.getRuntime().availableProcessors()*2;

    private volatile boolean released;
    private ServerBootstrap bootstrap;

    private Channel serverChannel;

    private MessageListener listener;


    public PinpointServerSocket() {
        ServerBootstrap bootstrap = createBootStrap(1, WORKER_COUNT);
        setOptions(bootstrap);
        addPipeline(bootstrap);
        this.bootstrap = bootstrap;
    }

    private void addPipeline(ServerBootstrap bootstrap) {
        ServerPipelineFactory serverPipelineFactory = new ServerPipelineFactory();
        bootstrap.setPipelineFactory(serverPipelineFactory);
    }

    public void setPipelineFactory(ChannelPipelineFactory channelPipelineFactory) {
        if (channelPipelineFactory ==null) {
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

    public void messageReceived() {

    }


    public void bind(String host, int port) throws SocketException {
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

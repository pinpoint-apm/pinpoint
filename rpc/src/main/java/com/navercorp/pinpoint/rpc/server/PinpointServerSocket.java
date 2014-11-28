package com.nhn.pinpoint.rpc.server;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.jboss.netty.bootstrap.ServerBootstrap;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.ChannelFutureListener;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.channel.ChannelStateEvent;
import org.jboss.netty.channel.ExceptionEvent;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelHandler;
import org.jboss.netty.channel.group.ChannelGroup;
import org.jboss.netty.channel.group.ChannelGroupFuture;
import org.jboss.netty.channel.group.ChannelGroupFutureListener;
import org.jboss.netty.channel.group.DefaultChannelGroup;
import org.jboss.netty.channel.socket.nio.NioServerBossPool;
import org.jboss.netty.channel.socket.nio.NioServerSocketChannelFactory;
import org.jboss.netty.channel.socket.nio.NioWorkerPool;
import org.jboss.netty.util.ThreadNameDeterminer;
import org.jboss.netty.util.Timeout;
import org.jboss.netty.util.Timer;
import org.jboss.netty.util.TimerTask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.nhn.pinpoint.common.util.PinpointThreadFactory;
import com.nhn.pinpoint.rpc.PinpointSocketException;
import com.nhn.pinpoint.rpc.client.WriteFailFutureListener;
import com.nhn.pinpoint.rpc.control.ProtocolException;
import com.nhn.pinpoint.rpc.packet.ControlHandshakePacket;
import com.nhn.pinpoint.rpc.packet.ControlHandshakeResponsePacket;
import com.nhn.pinpoint.rpc.packet.HandshakeResponseCode;
import com.nhn.pinpoint.rpc.packet.HandshakeResponseType;
import com.nhn.pinpoint.rpc.packet.Packet;
import com.nhn.pinpoint.rpc.packet.PacketType;
import com.nhn.pinpoint.rpc.packet.PingPacket;
import com.nhn.pinpoint.rpc.packet.RequestPacket;
import com.nhn.pinpoint.rpc.packet.ResponsePacket;
import com.nhn.pinpoint.rpc.packet.SendPacket;
import com.nhn.pinpoint.rpc.packet.ServerClosePacket;
import com.nhn.pinpoint.rpc.packet.stream.StreamPacket;
import com.nhn.pinpoint.rpc.stream.DisabledServerStreamChannelMessageListener;
import com.nhn.pinpoint.rpc.stream.ServerStreamChannelMessageListener;
import com.nhn.pinpoint.rpc.stream.StreamChannelManager;
import com.nhn.pinpoint.rpc.util.AssertUtils;
import com.nhn.pinpoint.rpc.util.ControlMessageEncodingUtils;
import com.nhn.pinpoint.rpc.util.CpuUtils;
import com.nhn.pinpoint.rpc.util.IDGenerator;
import com.nhn.pinpoint.rpc.util.LoggerFactorySetup;
import com.nhn.pinpoint.rpc.util.TimerFactory;

/**
 * @author emeroad
 * @author koo.taejin
 */
public class PinpointServerSocket extends SimpleChannelHandler {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());
//    private final boolean isDebug = logger.isDebugEnabled();

	private static final long DEFAULT_TIMEOUTMILLIS = 3 * 1000;
    private static final int WORKER_COUNT = CpuUtils.workerCount();

    private volatile boolean released;
    private ServerBootstrap bootstrap;

    private Channel serverChannel;
    private final ChannelGroup channelGroup = new DefaultChannelGroup(); 
    private final Timer pingTimer;
    private final Timer requestManagerTimer;

    private ServerMessageListener messageListener = SimpleLoggingServerMessageListener.LISTENER;
    private ServerStreamChannelMessageListener serverStreamChannelMessageListener = DisabledServerStreamChannelMessageListener.INSTANCE;
    
    private WriteFailFutureListener traceSendAckWriteFailFutureListener = new  WriteFailFutureListener(logger, "TraceSendAckPacket send fail.", "TraceSendAckPacket send() success.");
    private InetAddress[] ignoreAddressList;

    private final SocketChannelStateChangeEventListener channelStateChangeEventListener;
    
    static {
        LoggerFactorySetup.setupSlf4jLoggerFactory();
    }

    public PinpointServerSocket() {
    	this(DoNothingChannelStateEventListener.INSTANCE);
    }

    public PinpointServerSocket(SocketChannelStateChangeEventListener channelStateChangeEventListener) {
        ServerBootstrap bootstrap = createBootStrap(1, WORKER_COUNT);
        setOptions(bootstrap);
        addPipeline(bootstrap);
        this.bootstrap = bootstrap;
        this.pingTimer = TimerFactory.createHashedWheelTimer("PinpointServerSocket-PingTimer", 50, TimeUnit.MILLISECONDS, 512);
        this.requestManagerTimer = TimerFactory.createHashedWheelTimer("PinpointServerSocket-RequestManager", 50, TimeUnit.MILLISECONDS, 512);

        this.channelStateChangeEventListener = channelStateChangeEventListener;
    }
    
    
    public void setIgnoreAddressList(InetAddress[] ignoreAddressList) {
        if (ignoreAddressList == null) {
            throw new NullPointerException("ignoreAddressList must not be null");
        }
        this.ignoreAddressList = ignoreAddressList;
    }

    private void addPipeline(ServerBootstrap bootstrap) {
        ServerPipelineFactory serverPipelineFactory = new ServerPipelineFactory(this);
        bootstrap.setPipelineFactory(serverPipelineFactory);
    }

    void setPipelineFactory(ChannelPipelineFactory channelPipelineFactory) {
        if (channelPipelineFactory == null) {
            throw new NullPointerException("channelPipelineFactory must not be null");
        }
        bootstrap.setPipelineFactory(channelPipelineFactory);
    }

    public void setMessageListener(ServerMessageListener messageListener) {
        if (messageListener == null) {
            throw new NullPointerException("messageListener must not be null");
        }
        this.messageListener = messageListener;
    }
    
    public void setServerStreamChannelMessageListener(ServerStreamChannelMessageListener serverStreamChannelMessageListener) {
    	AssertUtils.assertNotNull(serverStreamChannelMessageListener, "serverStreamChannelMessageListener must not be null");
    	
        this.serverStreamChannelMessageListener = serverStreamChannelMessageListener;
    }

    private void setOptions(ServerBootstrap bootstrap) {
        // read write timeout이 있어야 되나? nio라서 없어도 되던가?
        // write timeout은 별도 interceptor를 통해서 이루어 져야 함. write timeout은 있음.

        // tcp 세팅
        bootstrap.setOption("child.tcpNoDelay", true);
        bootstrap.setOption("child.keepAlive", true);
        // buffer
        bootstrap.setOption("child.sendBufferSize", 1024 * 64);
        bootstrap.setOption("child.receiveBufferSize", 1024 * 64);

//        bootstrap.setOption("child.soLinger", 0);


    }

    private short getPacketType(Object packet) {
    	if (packet == null) {
    		return PacketType.UNKNOWN;
    	}
    	
    	if (packet instanceof Packet) {
    		return ((Packet) packet).getPacketType();
    	}
    	
		return PacketType.UNKNOWN;
    }

    private ServerBootstrap createBootStrap(int bossCount, int workerCount) {
        // profiler, collector,
        ExecutorService boss = Executors.newCachedThreadPool(new PinpointThreadFactory("Pinpoint-Server-Boss"));
        NioServerBossPool nioServerBossPool = new NioServerBossPool(boss, bossCount, ThreadNameDeterminer.CURRENT);

        ExecutorService worker = Executors.newCachedThreadPool(new PinpointThreadFactory("Pinpoint-Server-Worker"));
        NioWorkerPool nioWorkerPool = new NioWorkerPool(worker, workerCount, ThreadNameDeterminer.CURRENT);

        NioServerSocketChannelFactory nioClientSocketChannelFactory = new NioServerSocketChannelFactory(nioServerBossPool, nioWorkerPool);
        return new ServerBootstrap(nioClientSocketChannelFactory);
    }

    @Override
    public void messageReceived(ChannelHandlerContext ctx, MessageEvent e) throws Exception {
    	final Channel channel = e.getChannel();
    	Object message = e.getMessage();
    	
    	logger.debug("messageReceived:{} channel:{}", message, channel);
    	
    	ChannelContext channelContext = getChannelContext(channel);
    	if (!PinpointServerSocketStateCode.isRun(channelContext.getCurrentStateCode())) {
    		logger.warn("MessageReceived:{} from IllegalState Channel:{} this message will be ignore.", message, channel);
    		return;
    	}
    	
		final short packetType = getPacketType(message);
		switch (packetType) {
			case PacketType.APPLICATION_SEND: {
				SocketChannel socketChannel = getChannelContext(channel).getSocketChannel();
				messageListener.handleSend((SendPacket) message, socketChannel);
				return;
			}
//                case PacketType.APPLICATION_TRACE_SEND: {
//                    SocketChannel socketChannel = getChannelContext(channel).getSocketChannel();
//                    TraceSendPacket traceSendPacket = (TraceSendPacket) message;
//                    try {
//                        messageListener.handleSend(traceSendPacket, socketChannel);
//                    } finally {
//                        TraceSendAckPacket traceSendAckPacket = new TraceSendAckPacket(traceSendPacket.getTransactionId());
//                        ChannelFuture write = channel.write(traceSendAckPacket);
//                        write.addListener(traceSendAckWriteFailFutureListener);
//                    }
//                    return;
//                }
			case PacketType.APPLICATION_REQUEST: {
				SocketChannel socketChannel = getChannelContext(channel).getSocketChannel();
				messageListener.handleRequest((RequestPacket) message, socketChannel);
				return;
			}
			case PacketType.APPLICATION_RESPONSE: {
				SocketChannel socketChannel = getChannelContext(channel).getSocketChannel();
				socketChannel.receiveResponsePacket((ResponsePacket) message);
				return;
			}
			case PacketType.APPLICATION_STREAM_CREATE:
			case PacketType.APPLICATION_STREAM_CLOSE:
			case PacketType.APPLICATION_STREAM_CREATE_SUCCESS:
			case PacketType.APPLICATION_STREAM_CREATE_FAIL:
			case PacketType.APPLICATION_STREAM_RESPONSE:
			case PacketType.APPLICATION_STREAM_PING:
			case PacketType.APPLICATION_STREAM_PONG:
				handleStreamPacket((StreamPacket) message, channel);
				return;
			case PacketType.CONTROL_HANDSHAKE:
				int requestId = ((ControlHandshakePacket)message).getRequestId();
				
				Map<Object, Object> properties = decodeSocketProperties((ControlHandshakePacket) message);
				if (properties == null) {
					sendHandshakeResponseMessage(requestId, HandshakeResponseType.ProtocolError.PROTOCOL_ERROR, channel);
					return;
				}
				
				HandshakeResponseCode code = messageListener.handleHandshake(properties);
				
				if (code.getCode() != HandshakeResponseType.Success.CODE) {
					sendHandshakeResponseMessage(requestId, code, channel);
				} else {
					boolean isSet = channelContext.setChannelProperties(properties);
					
					if (isSet) {
	                    if (code == HandshakeResponseCode.DUPLEX_COMMUNICATION) {
	                        changeStateToRunDuplexCommunication(code, channel);
	                    }
	                    sendHandshakeResponseMessage(requestId, code, channel);
					} else {
                        if (code == HandshakeResponseCode.DUPLEX_COMMUNICATION) {
                            sendHandshakeResponseMessage(requestId, HandshakeResponseCode.ALREADY_DUPLEX_COMMUNICATION, channel);
                        } else {
                            sendHandshakeResponseMessage(requestId, HandshakeResponseCode.ALREADY_SIMPLEX_COMMUNICATION, channel);
                        }
					}
					
				}
				return;
			case PacketType.CONTROL_CLIENT_CLOSE: {
				closeChannel(channel);
				return;
			}
			default:
				logger.warn("invalid messageReceived msg:{}, connection:{}", message, e.getChannel());
			}
    }

	private void closeChannel(Channel channel) {
        logger.debug("received ClientClosePacket {}", channel);
        ChannelContext channelContext = getChannelContext(channel);
        channelContext.changeStateBeingShutdown();
        
//      상대방이 닫는거에 반응해서 socket을 닫도록 하자.
//        channel.close();
    }

    private void handleStreamPacket(StreamPacket packet, Channel channel) {
        ChannelContext context = getChannelContext(channel);
        context.getStreamChannelManager().messageReceived(packet);
    }
    
	private Map<Object, Object> decodeSocketProperties(ControlHandshakePacket message) {
		try {
			byte[] payload = message.getPayload();
            Map<Object, Object> properties = (Map) ControlMessageEncodingUtils.decode(payload);
			return properties;
		} catch (ProtocolException e) {
			logger.warn(e.getMessage(), e);
		}
		
		return null;
	}
    
	private boolean changeStateToRunDuplexCommunication(HandshakeResponseCode returnCode, Channel channel) {
		ChannelContext context = getChannelContext(channel);

		if (returnCode == HandshakeResponseType.Success.DUPLEX_COMMUNICATION) {
			if (context.getCurrentStateCode() != PinpointServerSocketStateCode.RUN_DUPLEX_COMMUNICATION) {
				context.changeStateRunDuplexCommunication();
				return true;
			} 
		}

		return false;
	}
	
    private void sendHandshakeResponseMessage(int requestId, HandshakeResponseCode handShakeResponseCode, Channel channel) {
		try {
	        logger.info("write HandshakeResponsePakcet. channel:{}, HandshakeResponseCode:{}.", channel, handShakeResponseCode);
		    
			Map<String, Object> result = new HashMap<String, Object>();
			result.put(ControlHandshakeResponsePacket.CODE, handShakeResponseCode.getCode());
            result.put(ControlHandshakeResponsePacket.SUB_CODE, handShakeResponseCode.getSubCode());
			
			byte[] resultPayload = ControlMessageEncodingUtils.encode(result);
			ControlHandshakeResponsePacket packet = new ControlHandshakeResponsePacket(requestId, resultPayload);
	
			channel.write(packet);
		} catch (ProtocolException e) {
			logger.warn(e.getMessage(), e);
		}
    }

    @Override
    public void channelOpen(ChannelHandlerContext ctx, ChannelStateEvent e) throws Exception {
        final Channel channel = e.getChannel();
        if (logger.isDebugEnabled()) {
            logger.debug("server channelOpen {}", channel);
        }
        super.channelOpen(ctx, e);
    }

    @Override
    public void channelConnected(ChannelHandlerContext ctx, ChannelStateEvent e) throws Exception {
        final Channel channel = e.getChannel();
        if (logger.isDebugEnabled()) {
            logger.debug("server channelConnected {}", channel);
        }
        if (released) {
            logger.warn("already released. channel:{}", channel);
            channel.write(new ServerClosePacket()).addListener(new ChannelFutureListener() {
                @Override
                public void operationComplete(ChannelFuture future) throws Exception {
                    future.getChannel().close();
                }
            });
            return;
        }
        prepareChannel(channel);
        
        ChannelContext channelContext = getChannelContext(channel);
        
        boolean check = checkIgnoreAddress(channel);
        if (check) {
        	channelContext.changeStateRun();
        }
        
        super.channelConnected(ctx, e);
    }

    @Override
    public void channelDisconnected(ChannelHandlerContext ctx, ChannelStateEvent e) throws Exception {
        final Channel channel = e.getChannel();
        final ChannelContext channelContext = getChannelContext(channel);
        PinpointServerSocketStateCode currentStateCode = channelContext.getCurrentStateCode();
        
        if (currentStateCode == PinpointServerSocketStateCode.BEING_SHUTDOWN) {
        	channelContext.changeStateShutdown();
        } else if(released) {
            channelContext.changeStateShutdown();
        } else {
            boolean check = checkIgnoreAddress(channel);
            if (check) {
            	channelContext.changeStateUnexpectedShutdown();
            }
        }
        
        if (logger.isDebugEnabled()) {
            logger.debug("server channelDisconnected {}", channel);
        }
        this.channelGroup.remove(channel);
        super.channelDisconnected(ctx, e);
    }

    // 참고 ChannelClose 이벤트는 상대방이 먼저 연결을 끊어 Disconnected가 발생했을 경우에도 발생이 가능함
    // 이부분 염두하고 코드 작성이 필요함 
    @Override
    public void channelClosed(ChannelHandlerContext ctx, ChannelStateEvent e) throws Exception {
        final Channel channel = e.getChannel();
        final ChannelContext channelContext = getChannelContext(channel);
        PinpointServerSocketStateCode currentStateCode = channelContext.getCurrentStateCode();
        
        if (currentStateCode == PinpointServerSocketStateCode.BEING_SHUTDOWN) {
            if (logger.isDebugEnabled()) {
                logger.debug("client channelClosed. normal closed. {}", channel);
            }
            channelContext.changeStateShutdown();
        } else if(released) {
            if (logger.isDebugEnabled()) {
                logger.debug("client channelClosed. server shutdown. {}", channel);
            }
            channelContext.changeStateShutdown();
        } else {
            boolean check = checkIgnoreAddress(channel);
            if (check) {
                logger.warn("Unexpected Client channelClosed {}", channel);
                channelContext.changeStateUnexpectedShutdown();
            } else {
                logger.debug("checkAddress, Client channelClosed channelClosed {}", channel);
            }
        }
        channelContext.closeAllStreamChannel();
    }

    private boolean checkIgnoreAddress(Channel channel) {
        if (ignoreAddressList == null) {
            return true;
        }
        final InetSocketAddress remoteAddress = (InetSocketAddress) channel.getRemoteAddress();
        if (remoteAddress == null) {
            return true;
        }
        InetAddress address = remoteAddress.getAddress();
        for (InetAddress ignore : ignoreAddressList) {
            if (ignore.equals(address)) {
                return false;
            }
        }
        return true;
    }

    private ChannelContext getChannelContext(Channel channel) {
        return (ChannelContext) channel.getAttachment();
    }

    private void prepareChannel(Channel channel) {
    	SocketChannel socketChannel = new SocketChannel(channel, DEFAULT_TIMEOUTMILLIS, requestManagerTimer);
       	StreamChannelManager streamChannelManager = new StreamChannelManager(channel, IDGenerator.createEvenIdGenerator(), serverStreamChannelMessageListener);
    	
        ChannelContext channelContext = new ChannelContext(socketChannel, streamChannelManager, channelStateChangeEventListener);

        channel.setAttachment(channelContext);

        channelGroup.add(channel);
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
        sendPing();
    }

    private void sendPing() {
        logger.debug("sendPing");
        final TimerTask pintTask = new TimerTask() {
            @Override
            public void run(Timeout timeout) throws Exception {
                if (timeout.isCancelled()) {
                    newPingTimeout(this);
                    return;
                }

                final ChannelGroupFuture write = channelGroup.write(PingPacket.PING_PACKET);
                if (logger.isWarnEnabled()) {
                    write.addListener(new ChannelGroupFutureListener() {
                        private final ChannelFutureListener listener = new WriteFailFutureListener(logger, "ping write fail", "ping write success");
                        @Override
                        public void operationComplete(ChannelGroupFuture future) throws Exception {

                            if (logger.isWarnEnabled()) {
                                for (ChannelFuture channelFuture : future) {
                                    channelFuture.addListener(listener);
                                }
                            }
                        }
                    });
                }
                newPingTimeout(this);
            }
        };
        newPingTimeout(pintTask);
    }

    private void newPingTimeout(TimerTask pintTask) {
        try {
            logger.debug("newPingTimeout");
            pingTimer.newTimeout(pintTask, 1000 * 60 * 5, TimeUnit.MILLISECONDS);
        } catch (IllegalStateException e) {
            // timer가 stop일 경우 정지.
            logger.debug("timer stopped. Caused:{}", e.getMessage());
        }
    }


    public void close() {
        synchronized (this) {
            if (released) {
                return;
            }
            released = true;
        }
        sendServerClosedPacket();

        pingTimer.stop();
        
        if (serverChannel != null) {
            ChannelFuture close = serverChannel.close();
            close.awaitUninterruptibly(3000, TimeUnit.MILLISECONDS);
            serverChannel = null;
        }
        if (bootstrap != null) {
            bootstrap.releaseExternalResources();
            bootstrap = null;
        }
        
        // 요청을 죽인뒤에 timer를 제거함
        requestManagerTimer.stop();
    }

    private void sendServerClosedPacket() {
        logger.info("sendServerClosedPacket start");
        final ChannelGroupFuture write = this.channelGroup.write(new ServerClosePacket());
        write.awaitUninterruptibly(5000, TimeUnit.MILLISECONDS);
        if (logger.isWarnEnabled()) {
            write.addListener(new ChannelGroupFutureListener() {
                private final ChannelFutureListener listener = new WriteFailFutureListener(logger, "serverClosePacket write fail", "serverClosePacket write success");

                @Override
                public void operationComplete(ChannelGroupFuture future) throws Exception {
                    for (ChannelFuture channelFuture : future) {
                        channelFuture.addListener(listener);
                    }
                }
            });
        }
        logger.info("sendServerClosedPacket end");
    }
    
    public List<ChannelContext> getDuplexCommunicationChannelContext() {
    	List<ChannelContext> channelContextList = new ArrayList<ChannelContext>();

        for (Channel channel : channelGroup) {
            ChannelContext context = getChannelContext(channel);

            if (context.getCurrentStateCode() == PinpointServerSocketStateCode.RUN_DUPLEX_COMMUNICATION) {
                channelContextList.add(context);
            }
        }
    	
    	return channelContextList;
    }
    
}


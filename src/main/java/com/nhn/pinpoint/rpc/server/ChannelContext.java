package com.nhn.pinpoint.rpc.server;

import org.jboss.netty.channel.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author emeroad
 * @author koo.taejin
 */
public class ChannelContext {

	private final Logger logger = LoggerFactory.getLogger(this.getClass());
	
    private final ServerStreamChannelManager streamChannelManager;

    private final Channel channel;

    private final SocketChannel socketChannel;
    
    private final PinpointServerSocketState state;

    private AgentProperties agentProperties;

    public ChannelContext(Channel channel) {
        if (channel == null) {
            throw new NullPointerException("channel must not be null");
        }
        this.channel = channel;
        this.socketChannel = new SocketChannel(channel);
        this.streamChannelManager = new ServerStreamChannelManager(channel);
        this.state = new PinpointServerSocketState();
    }


    public ServerStreamChannel getStreamChannel(int channelId) {
        return streamChannelManager.findStreamChannel(channelId);
    }

    public ServerStreamChannel createStreamChannel(int channelId) {
        return streamChannelManager.createStreamChannel(channelId);
    }


    public void closeAllStreamChannel() {
        streamChannelManager.closeInternal();
    }

    public SocketChannel getSocketChannel() {
        return socketChannel;
    }

	public PinpointServerSocketState getState() {
		return state;
	}
	
	public AgentProperties getAgentProperties() {
		return agentProperties;
	}

	public boolean setAgentProperties(AgentProperties agentProperties) {
		if (agentProperties == null) {
			return false;
		}
		
		if (this.agentProperties == null) {
			this.agentProperties = agentProperties;
			return true;
		} 
		
		logger.warn("Already Register AgentProperties.({}).", this.agentProperties);
		return false;
	}

}

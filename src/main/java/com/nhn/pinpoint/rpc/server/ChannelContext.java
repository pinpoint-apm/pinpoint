package com.nhn.pinpoint.rpc.server;

import java.util.Collections;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.nhn.pinpoint.rpc.util.CopyUtils;
import com.nhn.pinpoint.rpc.util.MapUtils;

public class ChannelContext {

	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	private final ServerStreamChannelManager streamChannelManager;

	private final SocketChannel socketChannel;

	private final PinpointServerSocketState state;

	private final SocketChannelStateChangeEventListener stateChangeEventListener;
	
	private volatile Map agentProperties = Collections.EMPTY_MAP;

	public ChannelContext(SocketChannel socketChannel, ServerStreamChannelManager streamChannelManager) {
		this(socketChannel, streamChannelManager, DoNothingChannelStateEventListener.INSTANCE);
	}
	
	public ChannelContext(SocketChannel socketChannel, ServerStreamChannelManager streamChannelManager, SocketChannelStateChangeEventListener stateChangeEventListener) {
		this.socketChannel = socketChannel;
		this.streamChannelManager = streamChannelManager;

		this.stateChangeEventListener = stateChangeEventListener;
		
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

	public PinpointServerSocketStateCode getCurrentStateCode() {
		return state.getCurrentState();
	}

	public void changeStateRun() {
		logger.debug("Channel({}) state will be changed {}.", socketChannel, PinpointServerSocketStateCode.RUN);
		if (state.changeStateRun()) {
			stateChangeEventListener.eventPerformed(this, PinpointServerSocketStateCode.RUN);
		}
	}

	public void changeStateRunWithoutRegister() {
		logger.debug("Channel({}) state will be changed {}.", socketChannel, PinpointServerSocketStateCode.RUN_WITHOUT_REGISTER);
		if (state.changeStateRunWithoutRegister()) {
			stateChangeEventListener.eventPerformed(this, PinpointServerSocketStateCode.RUN_WITHOUT_REGISTER);
		}
	}

	public void changeStateBeingShutdown() {
		logger.debug("Channel({}) state will be changed {}.", socketChannel, PinpointServerSocketStateCode.BEING_SHUTDOWN);
		if (state.changeStateBeingShutdown()) {
			stateChangeEventListener.eventPerformed(this, PinpointServerSocketStateCode.BEING_SHUTDOWN);
		}
	}

	public void changeStateShutdown() {
		logger.debug("Channel({}) state will be changed {}.", socketChannel, PinpointServerSocketStateCode.SHUTDOWN);
		if (state.changeStateShutdown()) {
			stateChangeEventListener.eventPerformed(this, PinpointServerSocketStateCode.SHUTDOWN);
		}
	}

	public void changeStateUnexpectedShutdown() {
		logger.debug("Channel({}) state will be changed {}.", socketChannel, PinpointServerSocketStateCode.UNEXPECTED_SHUTDOWN);
		if (state.changeStateUnexpectedShutdown()) {
			stateChangeEventListener.eventPerformed(this, PinpointServerSocketStateCode.UNEXPECTED_SHUTDOWN);
		}
	}

	public void changeStateUnkownError() {
		logger.debug("Channel({}) state will be changed {}.", socketChannel, PinpointServerSocketStateCode.ERROR_UNKOWN);
		if (state.changeStateUnkownError()) {
			stateChangeEventListener.eventPerformed(this, PinpointServerSocketStateCode.ERROR_UNKOWN);
		}
	}
	
	public String getVersion() {
		return MapUtils.get(agentProperties, AgentPropertiesType.VERSION.getName(), String.class, "UNKNOWN");
	}

	public Map getAgentProperties() {
		return agentProperties;
	}

	public boolean setAgentProperties(Map agentProperties) {
		if (agentProperties == null) {
			return false;
		}

		synchronized (agentProperties) {
			if (this.agentProperties == Collections.EMPTY_MAP) {
				this.agentProperties = Collections.unmodifiableMap(CopyUtils.mediumCopyMap(agentProperties));
				return true;
			}
		}

		logger.warn("Already Register AgentProperties.({}).", this.agentProperties);
		return false;
	}

}

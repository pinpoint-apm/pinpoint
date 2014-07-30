package com.nhn.pinpoint.rpc.server;

import java.util.Collections;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ChannelContext {

	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	private final ServerStreamChannelManager streamChannelManager;

	private final SocketChannel socketChannel;

	private final PinpointServerSocketState state;

	private Map agentProperties = Collections.EMPTY_MAP;

	public ChannelContext(SocketChannel socketChannel, ServerStreamChannelManager streamChannelManager) {
		this.socketChannel = socketChannel;
		this.streamChannelManager = streamChannelManager;

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
		state.changeStateRun();
	}

	public void changeStateRunWithoutRegister() {
		logger.debug("Channel({}) state will be changed {}.", socketChannel, PinpointServerSocketStateCode.RUN_WITHOUT_REGISTER);
		state.changeStateRunWithoutRegister();
	}

	public void changeStateBeingShutdown() {
		logger.debug("Channel({}) state will be changed {}.", socketChannel, PinpointServerSocketStateCode.BEING_SHUTDOWN);
		state.changeStateBeingShutdown();
	}

	public void changeStateShutdown() {
		logger.debug("Channel({}) state will be changed {}.", socketChannel, PinpointServerSocketStateCode.SHUTDOWN);
		state.changeStateShutdown();
	}

	public void changeStateUnexpectedShutdown() {
		logger.debug("Channel({}) state will be changed {}.", socketChannel, PinpointServerSocketStateCode.UNEXPECTED_SHUTDOWN);
		state.changeStateUnexpectedShutdown();
	}

	public void changeStateUnkownError() {
		logger.debug("Channel({}) state will be changed {}.", socketChannel, PinpointServerSocketStateCode.ERROR_UNKOWN);
		state.changeStateUnkownError();
	}

	public Map getAgentProperties() {
		return agentProperties;
	}

	public boolean setAgentProperties(Map agentProperties) {
		if (agentProperties == null) {
			return false;
		}

		if (this.agentProperties == Collections.EMPTY_MAP) {
			this.agentProperties = agentProperties;
			return true;
		}

		logger.warn("Already Register AgentProperties.({}).", this.agentProperties);
		return false;
	}

}

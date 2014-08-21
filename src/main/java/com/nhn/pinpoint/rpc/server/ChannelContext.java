package com.nhn.pinpoint.rpc.server;

import java.util.Collections;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.nhn.pinpoint.rpc.util.CopyUtils;

public class ChannelContext {

	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	private final ServerStreamChannelManager streamChannelManager;

	private final SocketChannel socketChannel;

	private final PinpointServerSocketState state;

	private final SocketChannelStateChangeEventListener stateChangeEventListener;
	
	private volatile Map<Object, Object> channelProperties = Collections.emptyMap();

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

	public void changeStateRunDuplexCommunication() {
		logger.debug("Channel({}) state will be changed {}.", socketChannel, PinpointServerSocketStateCode.RUN_DUPLEX_COMMUNICATION);
		if (state.changeStateRunDuplexCommunication()) {
			stateChangeEventListener.eventPerformed(this, PinpointServerSocketStateCode.RUN_DUPLEX_COMMUNICATION);
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

	public Map<Object, Object> getChannelProperties() {
		return channelProperties;
	}

	public boolean setChannelProperties(Map<Object, Object> properties) {
		if (properties == null) {
			return false;
		}
        if (this.channelProperties != null) {
            logger.warn("Already Register ChannelProperties.({}).", this.channelProperties);
            return false;
        }

        this.channelProperties = Collections.unmodifiableMap(properties);

		return false;
	}

}

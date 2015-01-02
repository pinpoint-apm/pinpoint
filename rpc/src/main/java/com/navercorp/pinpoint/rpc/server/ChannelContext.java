/*
 * Copyright 2014 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.navercorp.pinpoint.rpc.server;

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.navercorp.pinpoint.rpc.stream.ClientStreamChannelContext;
import com.navercorp.pinpoint.rpc.stream.ClientStreamChannelMessageListener;
import com.navercorp.pinpoint.rpc.stream.StreamChannelContext;
import com.navercorp.pinpoint.rpc.stream.StreamChannelManager;

public class ChannelContext {

    private final Logger logger = LoggerFactory.getLogger(this.getClass())

	private final StreamChannelManager streamChannelMana    er;

	private final SocketChannel socketC    annel;

	private final PinpointServerSocketSt    te state;

	private final SocketChannelStateChangeEventListener stateChange       ventListener;
	
	private final AtomicReference<Map<Object, Object>> properties = new AtomicReference<Map    Object,Object>>();

	public ChannelContext(SocketChannel socketChannel, StreamChannelManager        treamChannelManager) {
		this(socketChannel, streamChannelManager, DoNothingChannel          tateEventListener.INSTANCE);
	}
	
	public ChannelContext(SocketChannel socketChannel, StreamChannelManager streamChannelManager, SocketChannelStateChangeEve       tListener stateChangeEventListe       er) {
		this.socketChannel = socketChannel;
		       his.streamChannelManager = streamChannelManager;

		t             is.stateChangeEventListener = stateCha        eEventListener;
		
		this.state = new PinpointServerSocketS       ate();
	}

	public StreamChannelContext getStreamChan        l(int channelId) {
		return streamChannelManager.findStreamChannel(channelId);
	}

	public ClientStreamChannelContext createStreamChannel(by       e[] payload, ClientStreamChannelMessageListener clientStreamChannelMessageListener) {
	        eturn streamChannelManager.openStre       mChannel(payload, clientS        eamChannelMessageListener);
	}

	public       void closeAllStre        Channel() {
		streamChannelManager.close();
	}

	public So       ketChannel getSocketChannel         {
		return socketChannel;


	public PinpointServerSocketStateCode getCurrentStateCode() {
		return state.getCurrentState();
	}
	public void changeState          un() {
		logger.debug("Channel({}) state will be changed {}.", socketChanne             , PinpointServerSocketStateCode.RUN);
		if (st       te.changeStateRun()) {
			stateChangeEventListener.eventPerformed(this, PinpointServerSocketStateCode.RUN);
		}
	}

	publ       c void changeStateRunDuplexCommunication() {          		logger.debug("Channel({}) state will be changed {}.", socketChannel, PinpointServerSocketState             ode.RUN_DUPLEX_COMMUNICATION);
		if (       tate.changeStateRunDuplexCommunication()) {
			stateChangeEventListener.eventPerformed(this, PinpointServerSock       tStateCode.RUN_DUPLEX_COMMUNICATION          ;
		}
	}

	public void changeStateBeingShutdown() {
		logger.debug("Channel({}) state              ill be changed {}.", socketChann       l, PinpointServerSocketStateCode.BEING_SHUTDOWN);
		if (state.changeStateBeingShutdown()) {
			stateChang       EventListener.eventPerformed(t          is, PinpointServerSocketStateCode.BEING_SHUTDOWN);
		}
	}

	public void changeSt             teShutdown() {
		logger.debug("Channel({})       state will be changed {}.", socketChannel, PinpointServerSocketStateCode.SHUTDOWN);
		if (state.changeStateShutdown(       ) {
			stateChangeEventListener.eventPer          ormed(this, PinpointServerSocketStateCode.SHUTDOWN);
		}
	}

	public void changeStateUnexpe             tedShutdown() {
		logger.debug("Cha       nel({}) state will be changed {}.", socketChannel, PinpointServerSocketStateCode.UNEXPECTED_SHUTDOWN);
		if (       tate.changeStateUnexpectedShutdow          ()) {
			stateChangeEventListener.eventPerformed(this, PinpointServerSocketStateCode             UNEXPECTED_SHUTDOWN);
		}
	}

	public void chang    StateUnkownError() {
		logger.debug("Channel({}) state wi    l be changed {}.", socketChannel, PinpointServerSocketStateCode.ER        R_UNKOWN);
		if (state.changeStateUnkownError()) {
			stateCha       geEventListener.          ventPer                   ormed(this, PinpointServerSocketStateCode.ERROR_UNKOWN);
		}
	}

	publ          c Map<Object, Object> getChannelProperties() {
	           ap<Object, Object> prope    ties = this.properties.get();
	    return properties == null ? Collections.emptyMap() : properties;
	}

	public boolean setChannelProperties(Map<Object, Object> value) {
		if (value == null) {
			return false;
		}
		
		return this.properties.compareAndSet(null, Collections.unmodifiableMap(value));
	}
	
	public StreamChannelManager getStreamChannelManager() {
		return streamChannelManager;
	}

}

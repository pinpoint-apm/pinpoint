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

package com.navercorp.pinpoint.rpc.stream;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelFuture;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.navercorp.pinpoint.rpc.PinpointSocketException;
import com.navercorp.pinpoint.rpc.packet.PacketType;
import com.navercorp.pinpoint.rpc.packet.stream.BasicStreamPacket;
import com.navercorp.pinpoint.rpc.packet.stream.StreamClosePacket;
import com.navercorp.pinpoint.rpc.packet.stream.StreamCreateFailPacket;
import com.navercorp.pinpoint.rpc.packet.stream.StreamCreatePacket;
import com.navercorp.pinpoint.rpc.packet.stream.StreamCreateSuccessPacket;
import com.navercorp.pinpoint.rpc.packet.stream.StreamPacket;
import com.navercorp.pinpoint.rpc.packet.stream.StreamPingPacket;
import com.navercorp.pinpoint.rpc.packet.stream.StreamResponsePacket;
import com.navercorp.pinpoint.rpc.util.AssertUtils;
import com.navercorp.pinpoint.rpc.util.IDGenerator;

/**
 * @author koo.taejin
 */
public class StreamChannelManager {

    private final Logger logger = LoggerFactory.getLogger(this.getClass())

	private final Channel chan    el;

	private final IDGenerator idGen    rator;

	private final ServerStreamChannelMessageListener streamChannelMessag    Listener;

	private final ConcurrentMap<Integer, StreamChannelContext> channelMap = new ConcurrentHashMap<Integer, StreamChann    lContext>();

	public StreamChannelManager(Channel channel, IDGenerat       r idGenerator) {
		this(channel, idGenerator, DisabledServerStreamChannelMes        geListener.INSTANCE);
	}

	public StreamChannelManager(Channel channel, IDGenerator idGenerator, ServerStreamChannelMessageListener serverSt       eamChannelMessageListener) {
		AssertUtils.assertNotNull(ch       nnel, "Channel may not be null.");
		AssertUtils.assertNotNull(idGe       erator, "IDGenerator may not be null.");
		AssertUtils.assertNotNull(serverStreamChannelMessageListener, "ServerS             reamChannelMessa       eListener may not be null."       ;
		
		this.channel = channel;
		this.idGenerator = idGenerator;
		          his.streamChannelM       ssageListener = serverStreamChannelMes             ageListener;
	}
	
	pu          lic void close() {
		Set<Integer> keySet = channelMap.keyS                   t();
		
		for (Integer key : keySet) {
			clearResourceAndSendClose(key, BasicStreamPacket.CHANNEL_CLOSE);
		}
		
	}

	public ClientSt       eamChannelContext openStreamChannel(byte[] payload, ClientStreamChannelMessa       eListener clientStreamChannelMessageListener) {
       	logger.info("Open streamChannel initialization started. Channel:{} ", channel);

		final i       t streamChannelId = idGenerator.       enerate();

		ClientStreamChannel newStreamChannel = new ClientStreamChannel(channel, streamChannelId, this);
		newStreamChannel.change       tateOpen();

		ClientStreamChannelContext newStreamChannelContext = new ClientSt       eamChannelCont          xt(newStreamChannel, clientStreamChannelMessageListener);

		StreamChannelContext old = channelMap.put(str             amChannelId, newStreamChannelContext);
	       if (old != null) {
			throw new Pinp       intSocketException("already stream       hannelId exist:" + streamChann       lId + " streamChannel:" + old);
		}

		// the order of bel          w code is very important.
		newStreamChannel.changeStateOpenAwait();
		newStreamChannel.sendCreate(payload);

		newStreamCha          nel.awaitOpen(3000);

		i        (ne          StreamChannel.checkState(Strea          ChannelStateCode.RUN)) {
			lo          ger.info("Open streamChannel initialization completed. Channel:{}, StreamChnnelContext:{} ", channel, newStreamChannelContext)
			return newStreamChannelContext;
		} else {
       		newStreamChannel.changeStateClose();
			channelMap       remove(streamChannelId);

			throw new Pinpoi       tSocketException("Create StreamChannel failed.(channel:" + channel + ", StreamChannelId:" + streamChannelId + ")");
       	}
	}

	public void messageReceived(StreamPacket pack          t) {
		final int streamChannelId = p          c             et.getStreamChannelId();
		final short packetType = packet.ge       PacketType();

		l          gger.info("StreamChannel message received. (Channel:{             , StreamId:{}, Packet:{}).", channel, streamChannelId, packet);

		                           (PacketType.APPLICATION_STREAM_CREATE             == packetType) {
			handleCreate((StreamCreatePacket)           acket);
			return;
		}

		StreamChannelContex              context = findStreamChannel(streamChannelId);
		if (c          nt             xt == null) {
			if (!(PacketType.APPLICATION_STREAM_CLOSE == pack                      tType)) {
				clearResourceAndSendClose(streamChannelId, StreamClosePacket.ID_NO       _FOUND);
			}
		} else {
			if (isServerStre       mChannelContext(context)) {
				messageReceived((Serv       rStreamChannelCon          ext) context, packet);
			} else if             (isClientStreamChannelContext(context)) {
				                      essageReceived((ClientStreamChann             lContext) context, packet);
			} else {                      				clearResourceAndSendClose(str             amChannelId, StreamClosePacket.TY                      E             UNKOWN);
			}
		}
	}

	private void messageReceived(ServerStreamChann             lContext context, StreamPacket packet) {
		final short packetType = packet.getPacketType();
		final int st             eamChannelId = packet.getStreamChannelId();

		switch (packetType) {
			case PacketT       pe.APPLICATION_STREAM_CLOSE:
				handleStrea       Close(context, (StreamClosePacket)packet);
				break;       			case PacketTyp          .APPLICATION_STREAM_PING:
				handlePing(con             ext, (StreamPingPacket) packet);
				break;
			case Packe                      Type.APPLICATION_STREAM_PONG:
				// han             lePong((StreamPongPacket) packet);
				break;
			de                      ault:
				clearResourceAndSendClose(s             reamChannelId, StreamClosePacket.PACKET_UNKNOWN);
			                      logger.info("Unkown StreamPacket r             ceived Channel:{}, StreamId:{}, Packet;{}.", ch                      nnel, streamChannelId, packet);
	             }
	}

	private void messageReceived(Cli                      ntStreamChannelContext context, S             reamPacket packet) {
		final shor                                    acketType = packet.getPacketType();
		final int streamChannelId = pac             et.getStreamChannelId();

		switch (packetType) {
			case PacketType.APPLICATION_STREAM_CREATE_SUCCESS:
		             	handleCreateSuccess(context, (StreamCreateSuccessP       cket) packet);
				break;
			case PacketType.APPLICAT       ON_STREAM_CREATE_FAIL:
				handleCr       ateFail(context, (StreamCreateFailPacket) packet);
				break;
			case PacketType.APPLICATION_       TREAM_RESPONSE:
				handleStreamResponse(context, (StreamResponsePacket) packet);
				break;
       		case PacketType.APPLICATION_STREAM_CLOSE:
				       andleStreamClose(context, (StreamClo          ePacket) packet);
				break;
			case PacketType.APPLICATION_STREAM_PING:
				handlePing(context, (Str          amPingPack             t) packet);
				break;
             		case PacketType.APPLICAT                      ON_STRE          M_PONG:
				// handlePong((StreamPongPacket) pac             et);
				break;
			default:
				clearResourceAndSendClose(streamChannelId, StreamCl       sePacket.PACKET_UNKNOWN);
				logger.info("Unkown Str       amPacket received Channel:{}, StreamId:{}, Packet;{}.", channel, streamCh       nnelId, packet);
		}
	}

	private void handleCreate(StreamCreatePacket p          cket) {
		final int streamC          annelId = packet.getStreamChannelId();
		short code = BasicStreamPacket.SUCCESS
		ServerStreamChannel stre          mChannel = new ServerStreamCha          nel(this.channel, streamChannelId, thi             );
		ServerStreamChannelCont        t streamChannelContext = new ServerStreamChannelContext(streamChannel);

		code = registerStreamChannel(streamChann       lContext);

		if (code == BasicStreamPacket.SUCCESS) {
			code = s       reamChannelMessageListener.        ndleStreamCreate(streamChannelContext, (StreamCreatePacket) packet);

			if (code == 0) {
				streamChannel.c       angeStateRun();
				streamChannel.sendCreateSuccess();
			}        	}

		if (code != 0) {
			clearResourceAndSendCreateFail(streamChannelId, code);
		}
	}

	private        hort registerStreamChannel(ServerStreamChannelC       ntext streamChannelContext) {
		int streamChannelId =        treamChannelContext.getStreamId();
		ServerStreamChannel streamCh             nnel = streamChannelContext.getStreamChan          el();

		if (channelMap.putIfAbsent(streamChannelId, streamChannelContext) !        null) {
			streamChannel.changeStateClose();
			return St          eamCreateFailPacket.I       _DUP          ICATED;
		}

		if (!streamChannel.changeStateOpenArrived()) {
			strea             Channel.changeStateClose();
			channelMap.remove(streamChannelId);

			return StreamCreateF       ilPacket.STATE_ILLEGAL;
		}

		return BasicStreamPacket.SUCCESS;
	}

	private void handleCreateSucc       ss(ClientStreamChannelContext streamChannelCon          ext, StreamCreateSuccessPacket packet) {
		StreamChannel streamChannel = streamChannelConte       t.getStreamChannel();
		streamChannel.changeStateRun();
	}

       private void handleCreateFail(ClientStreamChan        lContext streamChannelContext, StreamCreateFailPacket packet) {
		clearStreamChannelResourc       (streamChannelContext.getStreamId());
	}

	priv       te void handleStreamResponse(ClientStreamChannelContext context, S       reamResponsePacket packet) {
		int streamChannelId = pac          et.getStreamChannelId();

		StreamChannel streamChannel = context.getS          r             amChannel();

		StreamChannelStateCode cu        entCode = streamChannel.getCurrentState();
		
		if (StreamCh       nnelStateCode.RUN == currentCode) {
			context.getClientStreamChannelMes       ageListener().handleStre        Data(context, packet);
		} else if (StreamChannelStateCode.OPEN_AWAIT == currentCode)       {
			// may happen in the timing
		} els        {
			clearResourceAndSendClose(streamCha        elId, StreamClosePacket.STATE_NOT_RUN);
		}
	}

	private void handleStreamClose(Cl       entStreamChannelContext context, StreamC       osePacket packet) {
		context.getCli        tStreamChannelMessageListener().handleStreamClose(con       ext, (StreamClosePacket) packet);
		clearStreamChannelResource(contex       .getStreamId());
	}
	
	private           oid handleStreamClose(ServerStreamChannelContext cont             xt, StreamClosePacket packet) {
		streamChannelMessageListener.handl       StreamClose(context, packet);
		clearStreamChannelResource(context.getStreamId       ));
	}

	private void handlePi        (StreamChannelContext streamChannelContext, StreamPingPacket pac       et) {
		int streamChannelId = packet.getStreamChannelId();

		Stream       hannel streamChannel = streamC        nnelContext.getStreamChannel();
		if (!streamChannel.checkState(StreamChan       elStateCode.RUN)) {
			clearResourceAndSendClose(streamChannelId, Stre          mCloseP             cket.S        TE_NOT_RUN);
			return;
		}

		streamChannel.sendPong(packet.getRequestId(       );
	}

	public StreamChannelContext findStreamChannel(int channelId) {          		Strea             Channe        ontext streamChannelContext = this.c       annelMap.get(channelId);

		return streamChannelContext;
	}

	private ChannelFuture cle    rResourceAndSendCreateFail(int streamChannelId, short code) {
		clearStreamChannelResource(streamChannelId);
		return sendCreateFail(streamChannelId, code);
	}

	protected ChannelFuture clearResourceAndSendClose(int streamChannelId, short code) {
		clearStreamChannelResource(streamChannelId);
		return sendClose(streamChannelId, code);
	}

	private void clearStreamChannelResource(int streamId) {
		StreamChannelContext streamChannelContext = channelMap.remove(streamId);

		if (streamChannelContext != null) {
			streamChannelContext.getStreamChannel().changeStateClose();
		}
	}

	private ChannelFuture sendCreateFail(int streamChannelId, short code) {
		StreamCreateFailPacket packet = new StreamCreateFailPacket(streamChannelId, code);
		return this.channel.write(packet);
	}

	private ChannelFuture sendClose(int streamChannelId, short code) {
		StreamClosePacket packet = new StreamClosePacket(streamChannelId, code);
		return this.channel.write(packet);
	}

	private boolean isServerStreamChannelContext(StreamChannelContext context) {
		if (context == null || !(context instanceof ServerStreamChannelContext)) {
			return false;
		}
		return true;
	}

	private boolean isClientStreamChannelContext(StreamChannelContext context) {
		if (context == null || !(context instanceof ClientStreamChannelContext)) {
			return false;
		}
		return true;
	}

	public boolean isSupportServerMode() {
		return streamChannelMessageListener != DisabledServerStreamChannelMessageListener.INSTANCE;
	}

}

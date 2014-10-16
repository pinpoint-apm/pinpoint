package com.nhn.pinpoint.rpc.stream;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelFuture;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.nhn.pinpoint.rpc.PinpointSocketException;
import com.nhn.pinpoint.rpc.packet.PacketType;
import com.nhn.pinpoint.rpc.packet.stream.BasicStreamPacket;
import com.nhn.pinpoint.rpc.packet.stream.StreamClosePacket;
import com.nhn.pinpoint.rpc.packet.stream.StreamCreateFailPacket;
import com.nhn.pinpoint.rpc.packet.stream.StreamCreatePacket;
import com.nhn.pinpoint.rpc.packet.stream.StreamCreateSuccessPacket;
import com.nhn.pinpoint.rpc.packet.stream.StreamPacket;
import com.nhn.pinpoint.rpc.packet.stream.StreamPingPacket;
import com.nhn.pinpoint.rpc.packet.stream.StreamResponsePacket;
import com.nhn.pinpoint.rpc.util.AssertUtils;
import com.nhn.pinpoint.rpc.util.IDGenerator;

/**
 * @author koo.taejin <kr14910>
 */
public class StreamChannelManager {

	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	private final Channel channel;

	private final IDGenerator idGenerator;

	private final ServerStreamChannelMessageListener streamChannelMessageListener;

	private final ConcurrentMap<Integer, StreamChannelContext> channelMap = new ConcurrentHashMap<Integer, StreamChannelContext>();

	public StreamChannelManager(Channel channel, IDGenerator idGenerator) {
		this(channel, idGenerator, DisabledServerStreamChannelMessageListener.INSTANCE);
	}

	public StreamChannelManager(Channel channel, IDGenerator idGenerator, ServerStreamChannelMessageListener serverStreamChannelMessageListener) {
		AssertUtils.assertNotNull(channel, "Channel may not be null.");
		AssertUtils.assertNotNull(idGenerator, "IDGenerator may not be null.");
		AssertUtils.assertNotNull(serverStreamChannelMessageListener, "ServerStreamChannelMessageListener may not be null.");
		
		this.channel = channel;
		this.idGenerator = idGenerator;
		this.streamChannelMessageListener = serverStreamChannelMessageListener;
	}
	
	public void close() {
		Set<Integer> keySet = channelMap.keySet();
		
		for (Integer key : keySet) {
			clearResourceAndSendClose(key, BasicStreamPacket.CHANNEL_CLOSE);
		}
		
	}

	public ClientStreamChannelContext openStreamChannel(byte[] payload, ClientStreamChannelMessageListener clientStreamChannelMessageListener) {
		logger.info("Open streamChannel initialization started. Channel:{} ", channel);

		final int streamChannelId = idGenerator.generate();

		ClientStreamChannel newStreamChannel = new ClientStreamChannel(channel, streamChannelId, this);
		newStreamChannel.changeStateOpen();

		ClientStreamChannelContext newStreamChannelContext = new ClientStreamChannelContext(newStreamChannel, clientStreamChannelMessageListener);

		StreamChannelContext old = channelMap.put(streamChannelId, newStreamChannelContext);
		if (old != null) {
			throw new PinpointSocketException("already streamChannelId exist:" + streamChannelId + " streamChannel:" + old);
		}

		// 이타이밍이 중요함 해당 로직보다 메시지가 빨리 도착하면 상태값이 꺠짐
		newStreamChannel.changeStateOpenAwait();
		newStreamChannel.sendCreate(payload);

		newStreamChannel.awaitOpen(3000);

		if (newStreamChannel.checkState(StreamChannelStateCode.RUN)) {
			logger.info("Open streamChannel initialization completed. Channel:{}, StreamChnnelContext:{} ", channel, newStreamChannelContext);
			return newStreamChannelContext;
		} else {
			newStreamChannel.changeStateClose();
			channelMap.remove(streamChannelId);

			throw new PinpointSocketException("Create Channel:" + channel + ", StreamChannelId:" + streamChannelId + " fail.");
		}
	}

	public void messageReceived(StreamPacket packet) {
		final int streamChannelId = packet.getStreamChannelId();
		final short packetType = packet.getPacketType();

		logger.info("StreamChannel message received. (Channel:{}, StreamId:{}, Packet:{}).", channel, streamChannelId, packet);

		if (PacketType.APPLICATION_STREAM_CREATE == packetType) {
			handleCreate((StreamCreatePacket) packet);
			return;
		}

		StreamChannelContext context = findStreamChannel(streamChannelId);
		if (context == null) {
			if (!(PacketType.APPLICATION_STREAM_CLOSE == packetType)) {
				clearResourceAndSendClose(streamChannelId, StreamClosePacket.ID_NOT_FOUND);
			}
		} else {
			if (isServerStreamChannelContext(context)) {
				messageReceived((ServerStreamChannelContext) context, packet);
			} else if (isClientStreamChannelContext(context)) {
				messageReceived((ClientStreamChannelContext) context, packet);
			} else {
				clearResourceAndSendClose(streamChannelId, StreamClosePacket.TYPE_UNKOWN);
			}
		}
	}

	private void messageReceived(ServerStreamChannelContext context, StreamPacket packet) {
		final short packetType = packet.getPacketType();
		final int streamChannelId = packet.getStreamChannelId();

		switch (packetType) {
			case PacketType.APPLICATION_STREAM_CLOSE:
				handleStreamClose(context, (StreamClosePacket)packet);;
				break;
			case PacketType.APPLICATION_STREAM_PING:
				handlePing(context, (StreamPingPacket) packet);
				break;
			case PacketType.APPLICATION_STREAM_PONG:
				// handlePong((StreamPongPacket) packet);
				break;
			default:
				clearResourceAndSendClose(streamChannelId, StreamClosePacket.PACKET_UNKNOWN);
				logger.info("Unkown StreamPacket received Channel:{}, StreamId:{}, Packet;{}.", channel, streamChannelId, packet);
		}
	}

	private void messageReceived(ClientStreamChannelContext context, StreamPacket packet) {
		final short packetType = packet.getPacketType();
		final int streamChannelId = packet.getStreamChannelId();

		switch (packetType) {
			case PacketType.APPLICATION_STREAM_CREATE_SUCCESS:
				handleCreateSuccess(context, (StreamCreateSuccessPacket) packet);
				break;
			case PacketType.APPLICATION_STREAM_CREATE_FAIL:
				handleCreateFail(context, (StreamCreateFailPacket) packet);
				break;
			case PacketType.APPLICATION_STREAM_RESPONSE:
				handleStreamResponse(context, (StreamResponsePacket) packet);
				break;
			case PacketType.APPLICATION_STREAM_CLOSE:
				handleStreamClose(context, (StreamClosePacket) packet);
				break;
			case PacketType.APPLICATION_STREAM_PING:
				handlePing(context, (StreamPingPacket) packet);
				break;
			case PacketType.APPLICATION_STREAM_PONG:
				// handlePong((StreamPongPacket) packet);
				break;
			default:
				clearResourceAndSendClose(streamChannelId, StreamClosePacket.PACKET_UNKNOWN);
				logger.info("Unkown StreamPacket received Channel:{}, StreamId:{}, Packet;{}.", channel, streamChannelId, packet);
		}
	}

	private void handleCreate(StreamCreatePacket packet) {
		final int streamChannelId = packet.getStreamChannelId();

		short code = BasicStreamPacket.SUCCESS;
		ServerStreamChannel streamChannel = new ServerStreamChannel(this.channel, streamChannelId, this);
		ServerStreamChannelContext streamChannelContext = new ServerStreamChannelContext(streamChannel);

		code = registerStreamChannel(streamChannelContext);

		if (code == BasicStreamPacket.SUCCESS) {
			code = streamChannelMessageListener.handleStreamCreate(streamChannelContext, (StreamCreatePacket) packet);

			if (code == 0) {
				streamChannel.changeStateRun();
				streamChannel.sendCreateSuccess();
			}
		}

		if (code != 0) {
			clearResourceAndSendCreateFail(streamChannelId, code);
		}
	}

	private short registerStreamChannel(ServerStreamChannelContext streamChannelContext) {
		int streamChannelId = streamChannelContext.getStreamId();
		ServerStreamChannel streamChannel = streamChannelContext.getStreamChannel();

		if (channelMap.putIfAbsent(streamChannelId, streamChannelContext) != null) {
			streamChannel.changeStateClose();
			return StreamCreateFailPacket.ID_DUPLICATED;
		}

		if (!streamChannel.changeStateOpenArrived()) {
			streamChannel.changeStateClose();
			channelMap.remove(streamChannelId);

			return StreamCreateFailPacket.STATE_ILLEGAL;
		}

		return BasicStreamPacket.SUCCESS;
	}

	private void handleCreateSuccess(ClientStreamChannelContext streamChannelContext, StreamCreateSuccessPacket packet) {
		StreamChannel streamChannel = streamChannelContext.getStreamChannel();
		streamChannel.changeStateRun();
	}

	private void handleCreateFail(ClientStreamChannelContext streamChannelContext, StreamCreateFailPacket packet) {
		clearStreamChannelResource(streamChannelContext.getStreamId());
	}

	private void handleStreamResponse(ClientStreamChannelContext context, StreamResponsePacket packet) {
		int streamChannelId = packet.getStreamChannelId();

		StreamChannel streamChannel = context.getStreamChannel();

		StreamChannelStateCode currentCode = streamChannel.getCurrentState();
		
		if (StreamChannelStateCode.RUN == currentCode) {
			context.getClientStreamChannelMessageListener().handleStreamData(context, packet);
		} else if (StreamChannelStateCode.OPEN_AWAIT == currentCode) {
			// 타이밍상 발생 가능
			
		} else {
			clearResourceAndSendClose(streamChannelId, StreamClosePacket.STATE_NOT_RUN);
		}
	}

	private void handleStreamClose(ClientStreamChannelContext context, StreamClosePacket packet) {
		context.getClientStreamChannelMessageListener().handleStreamClose(context, (StreamClosePacket) packet);
		clearStreamChannelResource(context.getStreamId());
	}
	
	private void handleStreamClose(ServerStreamChannelContext context, StreamClosePacket packet) {
		streamChannelMessageListener.handleStreamClose(context, packet);
		clearStreamChannelResource(context.getStreamId());
	}

	private void handlePing(StreamChannelContext streamChannelContext, StreamPingPacket packet) {
		int streamChannelId = packet.getStreamChannelId();

		StreamChannel streamChannel = streamChannelContext.getStreamChannel();
		if (!streamChannel.checkState(StreamChannelStateCode.RUN)) {
			clearResourceAndSendClose(streamChannelId, StreamClosePacket.STATE_NOT_RUN);
			return;
		}

		streamChannel.sendPong(packet.getRequestId());
	}

	public StreamChannelContext findStreamChannel(int channelId) {
		StreamChannelContext streamChannelContext = this.channelMap.get(channelId);

		return streamChannelContext;
	}

	private ChannelFuture clearResourceAndSendCreateFail(int streamChannelId, short code) {
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

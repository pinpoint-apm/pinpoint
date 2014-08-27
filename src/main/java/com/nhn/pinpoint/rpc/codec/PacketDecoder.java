package com.nhn.pinpoint.rpc.codec;

import com.nhn.pinpoint.rpc.client.WriteFailFutureListener;
import com.nhn.pinpoint.rpc.packet.*;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.handler.codec.frame.FrameDecoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author emeroad
 * @author koo.taejin
 */
public class PacketDecoder extends FrameDecoder {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private final WriteFailFutureListener pongWriteFutureListener = new WriteFailFutureListener(logger, "pong write fail.", "pong write success.");

    @Override
    protected Object decode(ChannelHandlerContext ctx, Channel channel, ChannelBuffer buffer) throws Exception {
        if (buffer.readableBytes() < 2) {
            return null;
        }
        buffer.markReaderIndex();
        final short packetType = buffer.readShort();
        switch (packetType) {
            case PacketType.APPLICATION_SEND:
                return readSend(packetType, buffer);
            case PacketType.APPLICATION_REQUEST:
                return readRequest(packetType, buffer);
            case PacketType.APPLICATION_RESPONSE:
                return readResponse(packetType, buffer);
            case PacketType.APPLICATION_STREAM_CREATE:
                return readStreamCreate(packetType, buffer);
            case PacketType.APPLICATION_STREAM_CLOSE:
                return readStreamClose(packetType, buffer);
            case PacketType.APPLICATION_STREAM_CREATE_SUCCESS:
                return readStreamCreateSuccess(packetType, buffer);
            case PacketType.APPLICATION_STREAM_CREATE_FAIL:
                return readStreamCreateFail(packetType, buffer);
            case PacketType.APPLICATION_STREAM_RESPONSE:
                return readStreamResponse(packetType, buffer);
            case PacketType.CONTROL_CLIENT_CLOSE:
                return readControlClientClose(packetType, buffer);
            case PacketType.CONTROL_SERVER_CLOSE:
                return readControlServerClose(packetType, buffer);
            case PacketType.CONTROL_PING:
                readPing(packetType, buffer);
                sendPong(channel);
                // 그냥 ping은 버리자.
                return null;
            case PacketType.CONTROL_PONG:
                logger.debug("receive pong. {}", channel);
                readPong(packetType, buffer);
                // pong 도 그냥 버리자.
                return null;
            case PacketType.CONTROL_ENABLE_WORKER:
            	return readEnableWorker(packetType, buffer);
            case PacketType.CONTROL_ENABLE_WORKER_CONFIRM:
            	return readEnableWorkerConfirm(packetType, buffer);
        }
        logger.error("invalid packetType received. packetType:{}, channel:{}", packetType, channel);
        channel.close();
        return null;
    }

	private void sendPong(Channel channel) {
        // ping에 대한 응답으로 pong은 자동으로 응답한다.
        logger.debug("receive ping. send pong. {}", channel);
        ChannelFuture write = channel.write(PongPacket.PONG_PACKET);
        write.addListener(pongWriteFutureListener);
    }


    private Object readControlClientClose(short packetType, ChannelBuffer buffer) {
        return ClientClosePacket.readBuffer(packetType, buffer);
    }

    private Object readControlServerClose(short packetType, ChannelBuffer buffer) {
        return ServerClosePacket.readBuffer(packetType, buffer);
    }

    private Object readPong(short packetType, ChannelBuffer buffer) {
        return PongPacket.readBuffer(packetType, buffer);
    }

    private Object readPing(short packetType, ChannelBuffer buffer) {
        return PingPacket.readBuffer(packetType, buffer);
    }


    private Object readSend(short packetType, ChannelBuffer buffer) {
        return SendPacket.readBuffer(packetType, buffer);
    }


    private Object readRequest(short packetType, ChannelBuffer buffer) {
        return RequestPacket.readBuffer(packetType, buffer);
    }

    private Object readResponse(short packetType, ChannelBuffer buffer) {
        return ResponsePacket.readBuffer(packetType, buffer);
    }



    private Object readStreamCreate(short packetType, ChannelBuffer buffer) {
        return StreamCreatePacket.readBuffer(packetType, buffer);
    }


    private Object readStreamCreateSuccess(short packetType, ChannelBuffer buffer) {
        return StreamCreateSuccessPacket.readBuffer(packetType, buffer);
    }

    private Object readStreamCreateFail(short packetType, ChannelBuffer buffer) {
        return StreamCreateFailPacket.readBuffer(packetType, buffer);
    }

    private Object readStreamResponse(short packetType, ChannelBuffer buffer) {
        return StreamResponsePacket.readBuffer(packetType, buffer);
    }

    private Object readStreamClose(short packetType, ChannelBuffer buffer) {
        return StreamClosePacket.readBuffer(packetType, buffer);
    }

    private Object readEnableWorker(short packetType, ChannelBuffer buffer) {
        return ControlEnableWorkerPacket.readBuffer(packetType, buffer);
	}

    private Object readEnableWorkerConfirm(short packetType, ChannelBuffer buffer) {
        return ControlEnableWorkerConfirmPacket.readBuffer(packetType, buffer);
	}

}

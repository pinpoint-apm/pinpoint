package com.nhn.pinpoint.common.rpc.message;

import com.nhn.pinpoint.common.io.rpc.packet.Packet;
import com.nhn.pinpoint.common.io.rpc.packet.PacketType;
import com.nhn.pinpoint.common.io.rpc.packet.SendPacket;
import org.jboss.netty.buffer.ChannelBuffer;
import org.junit.Assert;
import org.junit.Test;

/**
 *
 */
public class SendPacketTest {
    @Test
    public void testToBuffer() throws Exception {
        byte[] bytes = new byte[10];
        SendPacket packetSend = new SendPacket(bytes);

        ChannelBuffer channelBuffer = packetSend.toBuffer();

        short packetType = channelBuffer.readShort();
        SendPacket packet = (SendPacket) SendPacket.readBuffer(packetType, channelBuffer);
        Assert.assertArrayEquals(bytes, packet.getPayload());


    }
}

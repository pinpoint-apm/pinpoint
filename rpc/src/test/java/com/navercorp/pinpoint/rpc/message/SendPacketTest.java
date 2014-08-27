package com.nhn.pinpoint.rpc.message;

import com.nhn.pinpoint.rpc.packet.SendPacket;
import org.jboss.netty.buffer.ChannelBuffer;
import org.junit.Assert;
import org.junit.Test;

/**
 * @author emeroad
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

package com.navercorp.pinpoint.rpc.packet;

import junit.framework.Assert;

import org.jboss.netty.buffer.ChannelBuffer;
import org.junit.Test;

import com.navercorp.pinpoint.rpc.packet.PacketType;
import com.navercorp.pinpoint.rpc.packet.PongPacket;

/**
 * @author emeroad
 */
public class PongPacketTest {
    @Test
    public void testToBuffer() throws Exception {
        PongPacket pongPacket = new PongPacket();
        ChannelBuffer channelBuffer = pongPacket.toBuffer();

        short pongCode = channelBuffer.readShort();
        Assert.assertEquals(PacketType.CONTROL_PONG, pongCode);


    }
}

package com.nhn.pinpoint.common.io.rpc.message;

import com.nhn.pinpoint.common.io.rpc.packet.SendPacket;
import org.jboss.netty.buffer.ChannelBuffer;
import org.junit.Test;

/**
 *
 */
public class SendPacketTest {
    @Test
    public void testToBuffer() throws Exception {
        SendPacket packetSend = new SendPacket(new byte[10]);

        ChannelBuffer channelBuffer = packetSend.toBuffer();
        System.out.println(channelBuffer);
    }
}

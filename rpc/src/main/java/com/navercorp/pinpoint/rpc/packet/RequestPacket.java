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

package com.navercorp.pinpoint.rpc.packet;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;

/**
 * @author emeroad
 */
public class RequestPacket extends BasicPacket {

    private int requestId;

    public RequestPacket() {
    }

    public RequestPacket(byte[] payload) {
        super(payload);
    }

    public RequestPacket(int requestId, byte[] payload) {
        super(payload);
        this.requestId = requestId;
    }

    public int getRequestId() {
        return requestId;
    }

    public void setRequestId(int requestId) {
        this.requestId = requestId;
    }

    @Override
    public short getPacketType() {
        return PacketType.APPLICATION_REQUEST;
    }

    @Override
    public ChannelBuffer toBuffer() {

        ChannelBuffer header = ChannelBuffers.buffer(2 + 4 + 4);
        header.writeShort(PacketType.APPLICATION_REQUEST);
        header.writeInt(requestId);


        return PayloadPacket.appendPayload(header, payload);

    }


    public static RequestPacket readBuffer(short packetType, ChannelBuffer buffer) {
        assert packetType == PacketType.APPLICATION_REQUEST;

        if (buffer.readableBytes() < 8) {
            buffer.resetReaderIndex();
            return null;
        }

        final int messageId = buffer.readInt();
        final ChannelBuffer payload = PayloadPacket.readPayload(buffer);
        if (payload == null) {
            return null;
        }
        final RequestPacket requestPacket = new RequestPacket(payload.array());
        requestPacket.setRequestId(messageId);
        return requestPacket;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append("RequestPacket");
        sb.append("{requestId=").append(requestId);
        sb.append(", ");
        if (payload == null) {
            sb.append("payload=null");
        } else {
            sb.append("payloadLength=").append(payload.length);
        }
        sb.append('}');
        return sb.toString();
    }

}

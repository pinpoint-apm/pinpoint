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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author emeroad
 */
public class PayloadPacket {

    private static final Logger logger = LoggerFactory.getLogger(PayloadPacket.class);

    private static final ChannelBuffer EMPTY_BUFFER = ChannelBuffers.buffer(0);


    public static ChannelBuffer readPayload(ChannelBuffer buffer) {
        if (buffer.readableBytes() < 4) {
            buffer.resetReaderIndex();
            return null;
        }

        final int payloadLength = buffer.readInt();
        if (payloadLength <= 0) {
            return EMPTY_BUFFER;
        }

        if (buffer.readableBytes() < payloadLength) {
            buffer.resetReaderIndex();
            return null;
        }
        return buffer.readBytes(payloadLength);
    }


    public static ChannelBuffer appendPayload(final ChannelBuffer header, final byte[] payload) {
        if (payload == null) {
            // this is also payload header
            header.writeInt(-1);
            return header;
        } else {
            header.writeInt(payload.length);
            ChannelBuffer payloadWrap = ChannelBuffers.wrappedBuffer(payload);
            return ChannelBuffers.wrappedBuffer(true, header, payloadWrap);
        }
    }

}

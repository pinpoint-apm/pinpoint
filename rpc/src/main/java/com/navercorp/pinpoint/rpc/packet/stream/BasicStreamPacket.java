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

package com.navercorp.pinpoint.rpc.packet.stream;

/**
 * @author koo.taejin
 */
public abstract class BasicStreamPacket implements StreamPacket {

    private static final byte[] EMPTY_PAYLOAD = new byte[0];

    private final int streamChannelId;

    public BasicStreamPacket(int streamChannelId) {
        this.streamChannelId = streamChannelId;
    }

    public byte[] getPayload() {
        return EMPTY_PAYLOAD;
    }

    public int getStreamChannelId() {
        return streamChannelId;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append(this.getClass().getSimpleName());
        sb.append("{streamChannelId=").append(streamChannelId);
        sb.append(", ");
        if (getPayload() == null || getPayload() == EMPTY_PAYLOAD) {
            sb.append("payload=null");
        } else {
            sb.append("payloadLength=").append(getPayload().length);
        }
        sb.append('}');
        return sb.toString();
    }

}

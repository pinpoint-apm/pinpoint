/*
 * Copyright 2019 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.navercorp.pinpoint.rpc.stream;

import com.navercorp.pinpoint.rpc.packet.stream.StreamClosePacket;
import com.navercorp.pinpoint.rpc.packet.stream.StreamCode;
import com.navercorp.pinpoint.rpc.packet.stream.StreamCreatePacket;
import com.navercorp.pinpoint.rpc.packet.stream.StreamResponsePacket;

/**
 * @author Taejin Koo
 */
public abstract class ServerStreamChannelMessageHandler implements StreamChannelMessageHandler<ServerStreamChannel> {

    public static final ServerStreamChannelMessageHandler DISABLED_INSTANCE = new ServerStreamChannelMessageHandler.DisabledHandler();

    @Override
    public final void handleStreamResponsePacket(ServerStreamChannel streamChannel, StreamResponsePacket packet) {
        throw new UnsupportedOperationException("unsupported operation");
    }

    private static class DisabledHandler extends ServerStreamChannelMessageHandler {

        @Override
        public StreamCode handleStreamCreatePacket(ServerStreamChannel streamChannel, StreamCreatePacket packet) {
            return StreamCode.CONNECTION_UNSUPPORT;
        }

        @Override
        public void handleStreamClosePacket(ServerStreamChannel streamChannel, StreamClosePacket packet) {
        }

    }

}

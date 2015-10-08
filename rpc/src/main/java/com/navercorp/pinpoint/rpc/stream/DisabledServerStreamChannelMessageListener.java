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

package com.navercorp.pinpoint.rpc.stream;

import com.navercorp.pinpoint.rpc.packet.stream.StreamClosePacket;
import com.navercorp.pinpoint.rpc.packet.stream.StreamCode;
import com.navercorp.pinpoint.rpc.packet.stream.StreamCreatePacket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DisabledServerStreamChannelMessageListener implements ServerStreamChannelMessageListener {

    public static final ServerStreamChannelMessageListener INSTANCE = new DisabledServerStreamChannelMessageListener();

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Override
    public StreamCode handleStreamCreate(ServerStreamChannelContext streamChannelContext, StreamCreatePacket packet) {
        logger.info("{} handleStreamCreate unsupported operation. StreamChannel:{}, Packet:{}", this.getClass().getSimpleName(), streamChannelContext, packet);
        return StreamCode.CONNECTION_UNSUPPORT;
    }

    @Override
    public void handleStreamClose(ServerStreamChannelContext streamChannelContext, StreamClosePacket packet) {
        logger.info("{} handleStreamClose unsupported operation. StreamChannel:{}, Packet:{}", this.getClass().getSimpleName(), streamChannelContext, packet);
    }

}

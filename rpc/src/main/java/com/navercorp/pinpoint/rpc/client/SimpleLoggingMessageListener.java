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

package com.navercorp.pinpoint.rpc.client;

import org.jboss.netty.channel.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.navercorp.pinpoint.rpc.packet.RequestPacket;
import com.navercorp.pinpoint.rpc.packet.ResponsePacket;
import com.navercorp.pinpoint.rpc.packet.SendPacket;

public class SimpleLoggingMessageListener implements MessageListener {
    
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    public static final SimpleLoggingMessageListener LISTENER = new SimpleLoggingMessageListener()

	@Ove    ride
	public void handleSend(SendPacket sendPacket, Channel channel) {
        logger.info("handlerSend {} {}", sendPacket, c        nnel);
    }

	@Override
	public void handleRequest(RequestPacket requestPacket, C       annel channel) {
		channel.write(new ResponsePacket(requestPacket.getRequestId(), new byte[0]));
        logger.info("handlerRequest {} {}", r    questPacket, channel);
	}

}

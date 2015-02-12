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

package com.navercorp.pinpoint.rpc.server;

import com.navercorp.pinpoint.rpc.packet.RequestPacket;
import com.navercorp.pinpoint.rpc.packet.SendPacket;
import com.navercorp.pinpoint.rpc.server.handler.HandshakerHandler;

/**
 * @author emeroad
 */
public interface ServerMessageListener extends HandshakerHandler {

    void handleSend(SendPacket sendPacket, WritablePinpointServer pinpointServer);

    // TODO make another tcp channel in case of exposed channel.
    void handleRequest(RequestPacket requestPacket, WritablePinpointServer pinpointServer);

}

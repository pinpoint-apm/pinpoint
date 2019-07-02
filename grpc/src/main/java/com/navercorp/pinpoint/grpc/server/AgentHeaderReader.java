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

package com.navercorp.pinpoint.grpc.server;

import com.navercorp.pinpoint.common.util.Assert;
import com.navercorp.pinpoint.grpc.Header;
import com.navercorp.pinpoint.grpc.HeaderReader;
import io.grpc.Metadata;

/**
 * @author Woonduk Kang(emeroad)
 */
public class AgentHeaderReader implements HeaderReader<Header> {

    public AgentHeaderReader() {
    }

    @Override
    public Header extract(Metadata headers) {
        final String agentId = headers.get(Header.AGENT_ID_KEY);
        final String applicationName = headers.get(Header.APPLICATION_NAME_KEY);
        final String agentStartTimeStr = headers.get(Header.AGENT_START_TIME_KEY);
        Assert.requireNonNull(agentStartTimeStr, "agentStartTime must not be null");
        // check number format
        final long startTime = Long.parseLong(agentStartTimeStr);

        final long socketId = getSocketId(headers);

        return new Header(agentId, applicationName, startTime, socketId);
    }

    private long getSocketId(Metadata headers) {
        final String socketIdStr = headers.get(Header.SOCKET_ID);
        if (socketIdStr == null) {
            return Header.SOCKET_ID_NOT_EXIST;
        }
        try {
            return Long.parseLong(socketIdStr);
        } catch (NumberFormatException e) {
            return Header.SOCKET_ID_NOT_EXIST;
        }
    }



}

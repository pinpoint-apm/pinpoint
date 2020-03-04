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

import com.navercorp.pinpoint.grpc.Header;
import com.navercorp.pinpoint.grpc.HeaderReader;
import io.grpc.Metadata;
import io.grpc.StatusRuntimeException;
import org.junit.Assert;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * @author Woonduk Kang(emeroad)
 */
public class AgentHeaderReaderTest {

    private static final String AGENT_ID = "agentId";
    private static final String APPLICATION_NAME = "applicationName";
    private static final long AGENT_START_TIME = System.currentTimeMillis();
    private static final long SOCKET_ID = 1001;

    private HeaderReader<Header> reader = new AgentHeaderReader();

    @Test
    public void extract() {
        Metadata metadata = newMetadata();
        Header header = reader.extract(metadata);

        Assert.assertEquals(header.getAgentId(), AGENT_ID);
        Assert.assertEquals(header.getApplicationName(), APPLICATION_NAME);
        Assert.assertEquals(header.getAgentStartTime(), AGENT_START_TIME);
        Assert.assertEquals(header.getSocketId(), SOCKET_ID);
    }

    @Test(expected = StatusRuntimeException.class)
    public void extract_fail_agentId() {
        Metadata metadata = newMetadata();
        metadata.put(Header.AGENT_ID_KEY, "!!agentId");
        reader.extract(metadata);
    }

    @Test(expected = StatusRuntimeException.class)
    public void extract_fail_applicationName() {
        Metadata metadata = newMetadata();
        metadata.put(Header.APPLICATION_NAME_KEY, "!!applicationName");
        reader.extract(metadata);
    }

    private Metadata newMetadata() {
        Metadata metadata = new Metadata();
        metadata.put(Header.AGENT_ID_KEY, AGENT_ID);
        metadata.put(Header.APPLICATION_NAME_KEY, APPLICATION_NAME);
        metadata.put(Header.AGENT_START_TIME_KEY, Long.toString(AGENT_START_TIME));
        metadata.put(Header.SOCKET_ID, Long.toString(SOCKET_ID));
        return metadata;
    }
}
/*
 * Copyright 2018 NAVER Corp.
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

package com.navercorp.pinpoint.plugin.redis;

import com.navercorp.pinpoint.profiler.context.SpanEvent;
import com.navercorp.pinpoint.test.junit5.BasePinpointTest;
import com.navercorp.pinpoint.test.junit5.JunitAgentConfigPath;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import redis.clients.jedis.Client;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.Pipeline;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@JunitAgentConfigPath("pinpoint.config")
public class JedisPluginTest extends BasePinpointTest {

    private static final String HOST = "localhost";
    private static final int PORT = 6379;
    
    @Test
    public void jedis() {
        JedisMock jedis = new JedisMock("localhost", 6379);
        try {
            jedis.get("foo");
        } finally {
            close(jedis);
        }
        final List<SpanEvent> events = getCurrentSpanEvents();
        assertThat(events).hasSize(1);

        final SpanEvent eventBo = events.get(0);
        Assertions.assertEquals(HOST + ":" + PORT, eventBo.getEndPoint());
        Assertions.assertEquals("REDIS", eventBo.getDestinationId());

    }

    public void close(Jedis jedis) {
        if (jedis != null) {
            jedis.close();
        }
    }

    @Test
    public void binaryJedis() {
        JedisMock jedis = new JedisMock("localhost", 6379);
        try {
            jedis.get("foo".getBytes());
        } finally {
            close(jedis);
        }
        final List<SpanEvent> events = getCurrentSpanEvents();
        assertThat(events).hasSize(1);

        final SpanEvent eventBo = events.get(0);
        Assertions.assertEquals(HOST + ":" + PORT, eventBo.getEndPoint());
        Assertions.assertEquals("REDIS", eventBo.getDestinationId());
    }

    
    @Test
    public void pipeline() {
        JedisMock jedis = new JedisMock("localhost", 6379);
        try {
            Pipeline pipeline = jedis.pipelined();
            pipeline.get("foo");
        } finally {
            close(jedis);
        }
        
        final List<SpanEvent> events = getCurrentSpanEvents();
        assertThat(events).hasSize(1);
    }
    
    
    public class JedisMock extends Jedis {
        public JedisMock(String host, int port) {
            super(host, port);

            client = mock(Client.class);

            // for 'get' command
            when(client.isInMulti()).thenReturn(false);
            when(client.getBulkReply()).thenReturn("bar");
            when(client.getBinaryBulkReply()).thenReturn("bar".getBytes());
        }
    }

}

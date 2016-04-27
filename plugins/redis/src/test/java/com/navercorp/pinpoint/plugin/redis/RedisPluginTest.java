package com.navercorp.pinpoint.plugin.redis;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.Test;

import redis.clients.jedis.Client;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.Pipeline;

import com.navercorp.pinpoint.common.server.bo.SpanEventBo;
import com.navercorp.pinpoint.test.junit4.BasePinpointTest;

public class RedisPluginTest extends BasePinpointTest {

    private static final String HOST = "localhost";
    private static final int PORT = 6379;
    
    @Test
    public void jedis() {
        JedisMock jedis = new JedisMock("localhost", 6379);
        try {
            jedis.get("foo");
        } finally {
            if(jedis != null) {
                jedis.close();
            }
        }
        final List<SpanEventBo> events = getCurrentSpanEvents();
        assertEquals(1, events.size());
        
        final SpanEventBo eventBo = events.get(0);
        assertEquals(HOST + ":" + PORT, eventBo.getEndPoint());
        assertEquals("REDIS", eventBo.getDestinationId());
        
    }
    
    @Test
    public void binaryJedis() {
        JedisMock jedis = new JedisMock("localhost", 6379);
        try {
            jedis.get("foo".getBytes());
        } finally {
            if(jedis != null) {
                jedis.close();
            }
        }
        final List<SpanEventBo> events = getCurrentSpanEvents();
        assertEquals(1, events.size());
        
        final SpanEventBo eventBo = events.get(0);
        assertEquals(HOST + ":" + PORT, eventBo.getEndPoint());
        assertEquals("REDIS", eventBo.getDestinationId());
    }

    
    @Test
    public void pipeline() {
        JedisMock jedis = new JedisMock("localhost", 6379);
        try {
            Pipeline pipeline = jedis.pipelined();
            pipeline.get("foo");
        } finally {
            if(jedis != null) {
                jedis.close();
            }
        }
        
        final List<SpanEventBo> events = getCurrentSpanEvents();
        assertEquals(1, events.size());
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

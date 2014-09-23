package com.nhn.pinpoint.profiler.modifier.redis;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.util.List;

import org.junit.Before;
import org.junit.Test;

import redis.clients.jedis.exceptions.JedisDataException;

import com.nhn.pinpoint.common.ServiceType;
import com.nhn.pinpoint.common.bo.SpanEventBo;
import com.nhn.pinpoint.profiler.junit4.BasePinpointTest;
import com.nhncorp.redis.cluster.RedisCluster;
import com.nhncorp.redis.cluster.gateway.GatewayAddress;
import com.nhncorp.redis.cluster.gateway.GatewayClient;
import com.nhncorp.redis.cluster.gateway.GatewayConfig;
import com.nhncorp.redis.cluster.gateway.GatewayServer;
import com.nhncorp.redis.cluster.pipeline.RedisClusterPipeline;

public class RedisClusterPipelineModifierTest extends BasePinpointTest {
    private static final String HOST = "10.99.116.91";
    private static final int PORT = 6390;

    private RedisClusterPipeline pipeline;

    @Before
    public void before() {
        GatewayServer server = new GatewayServer(new GatewayAddress(HOST, PORT));
        pipeline = new RedisClusterPipeline(server);
    }

    @Test
    public void traceMethod() {
        pipeline.get("foo");
        pipeline.syncAndReturnAll();

        final List<SpanEventBo> spanEvents = getCurrentSpanEvents();
        for (SpanEventBo bo : spanEvents) {
            System.out.println("### " + bo);
        }

        assertEquals(2, spanEvents.size());
        SpanEventBo event = spanEvents.get(0);

        assertEquals(HOST, event.getDestinationId());
        assertEquals(HOST + ":" + PORT, event.getEndPoint());
        assertEquals(ServiceType.NBASE_ARC, event.getServiceType());
        assertNull(event.getExceptionMessage());
    }

}
package com.nhn.pinpoint.profiler.modifier.redis;

import static org.junit.Assert.*;

import java.util.List;

import org.junit.Before;
import org.junit.Test;

import redis.clients.jedis.exceptions.JedisDataException;

import com.nhn.pinpoint.common.ServiceType;
import com.nhn.pinpoint.common.bo.SpanEventBo;
import com.nhn.pinpoint.test.junit4.BasePinpointTest;
import com.nhncorp.redis.cluster.RedisCluster;
import com.nhncorp.redis.cluster.gateway.GatewayClient;
import com.nhncorp.redis.cluster.gateway.GatewayConfig;

public class RedisClusterModifierIT extends BasePinpointTest {
    private static final String HOST = "10.99.116.91";
    private static final int PORT = 6390;
    private static final String ZK_ADDRESS = "dev.xnbasearc.navercorp.com:2181";
    private static final String CLUSTER_NAME = "java_client_test";

    private RedisCluster redis;

    @Before
    public void before() {
        redis = new RedisCluster(HOST, PORT);
    }

    @Test
    public void traceMethod() {
        redis.get("foo");

        final List<SpanEventBo> spanEvents = getCurrentSpanEvents();
        assertEquals(1, spanEvents.size());
        SpanEventBo event = spanEvents.get(0);

        assertEquals("NBASE_ARC", event.getDestinationId());
        assertEquals(HOST + ":" + PORT, event.getEndPoint());
        assertEquals(ServiceType.NBASE_ARC, event.getServiceType());
        assertNull(event.getExceptionMessage());
    }
    
    @Test
    public void traceBinaryMethod() {
        redis.get("foo".getBytes());

        final List<SpanEventBo> spanEvents = getCurrentSpanEvents();
        assertEquals(1, spanEvents.size());
        SpanEventBo event = spanEvents.get(0);

        assertEquals("NBASE_ARC", event.getDestinationId());
        assertEquals(HOST + ":" + PORT, event.getEndPoint());
        assertEquals(ServiceType.NBASE_ARC, event.getServiceType());
        assertNull(event.getExceptionMessage());
    }

    @Test
    public void traceDestinationId() {
        GatewayConfig config = new GatewayConfig();
        config.setZkAddress(ZK_ADDRESS);
        config.setClusterName(CLUSTER_NAME);

        GatewayClient client = new GatewayClient(config);

        client.get("foo");

        final List<SpanEventBo> spanEvents = getCurrentSpanEvents();
        SpanEventBo event = spanEvents.get(spanEvents.size() - 1);

        assertEquals(CLUSTER_NAME, event.getDestinationId());
        assertEquals(HOST + ":" + PORT, event.getEndPoint());
        assertEquals(ServiceType.NBASE_ARC, event.getServiceType());
        assertNull(event.getExceptionMessage());

        client.destroy();
    }

    @Test
    public void traceMethodThrowException() {
        // 에러가 발생한 경우에 대한 event 결과를 확인한다.
        String key = null;
        try {
            redis.get(key);
        } catch (JedisDataException e) {
            // 값이 null인 것에 대한 에러를 던진다.
        }

        final List<SpanEventBo> spanEvents = getCurrentSpanEvents();
        assertEquals(1, spanEvents.size());
        SpanEventBo event = spanEvents.get(0);

        assertNotNull(event.getExceptionMessage());
    }
}
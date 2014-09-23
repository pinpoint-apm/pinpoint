
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

public class RedisClusterModifierTest extends BasePinpointTest {
    private static final String HOST = "10.99.116.91";
    private static final int PORT = 6390;

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

        assertEquals(HOST, event.getDestinationId());
        assertEquals(HOST + ":" + PORT, event.getEndPoint());
        assertEquals(ServiceType.NBASE_ARC, event.getServiceType());
        assertNull(event.getExceptionMessage());
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
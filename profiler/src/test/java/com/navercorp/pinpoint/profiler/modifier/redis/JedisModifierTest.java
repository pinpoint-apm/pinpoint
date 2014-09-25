package com.nhn.pinpoint.profiler.modifier.redis;

import static org.junit.Assert.*;

import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.exceptions.JedisDataException;

import com.nhn.pinpoint.common.ServiceType;
import com.nhn.pinpoint.common.bo.SpanEventBo;
import com.nhn.pinpoint.profiler.junit4.BasePinpointTest;

public class JedisModifierTest extends BasePinpointTest {
    private static final String HOST = "10.99.116.91";
    private static final int PORT = 6390;

    private Jedis jedis;

    @Before
    public void before() {
        // 생성자 인터셉터도 함께 테스트 해야 하는데 mock으로는 어려워서 실제 jedis를 사용한다.
        // jedis 주소는 nBase-ARC 개발환경의 redis 주소를 사용한다.
        jedis = new Jedis(HOST, PORT);
    }

    @After
    public void after() {
        if (jedis != null) {
            jedis.close();
        }
    }

    @Test
    public void traceMethod() {
        // get 명령을 실행하고 event 결과를 확인한다.
        jedis.get("foo");

        final List<SpanEventBo> spanEvents = getCurrentSpanEvents();
        assertEquals(1, spanEvents.size());
        SpanEventBo event = spanEvents.get(0);
        
        assertEquals(HOST + ":" + PORT, event.getEndPoint());
        assertEquals("REDIS", event.getDestinationId());
        assertEquals(ServiceType.REDIS, event.getServiceType());
        assertNull(event.getExceptionMessage());
    }

    @Test
    public void traceMethodThrowException() {
        // 에러가 발생한 경우에 대한 event 결과를 확인한다.
        String key = null;
        try {
            jedis.get(key);
        } catch (JedisDataException e) {
            // 값이 null인 것에 대한 에러를 던진다.
        }

        final List<SpanEventBo> spanEvents = getCurrentSpanEvents();
        assertEquals(1, spanEvents.size());
        SpanEventBo event = spanEvents.get(0);

        assertNotNull(event.getExceptionMessage());
    }
}

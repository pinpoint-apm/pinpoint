package com.navercorp.pinpoint.plugin.arcus;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

/**
 * @author harebox
 */
public class FrontCacheGetFutureModifierIntegrationTest /*extends BasePinpointTest*/ {

    @Before
    public void setUp() throws Exception {
//        MockitoAnnotations.initMocks(this);
    }

    @Test
    @Ignore
    // FIXME 테스트 깨짐
    public void frontCacheShouldBeTraced() throws Exception {
//        // given: front-cache-enabled ArcusClient
//        ConnectionFactoryBuilder cfb = new ConnectionFactoryBuilder();
//        cfb.setMaxFrontCacheElements(100);
//        cfb.setFrontCacheExpireTime(100);
//        ArcusClient client = ArcusClient.createArcusClient("ncloud.arcuscloud.nhncorp.com:17288", "ff31ddb85e9b431c8c0e5e50a4315c27", cfb);
//
//        // when
//        try {
//            client.set("hello", 0, "world");
//            client.asyncGet("hello").get();
//            client.asyncGet("hello").get();
//        } catch (Exception e) {
//            e.printStackTrace();
//            fail();
//        } finally {
//            client.shutdown();
//        }
//
//        // then
//        final List<SpanEventBo> spanEvents = getCurrentSpanEvents();
//        assertThat(spanEvents.size(), is(5));
//
//        final SpanEventBo getFutureSpan = spanEvents.get(2);
//        final SpanEventBo frontCacheGetFutureSpan = spanEvents.get(4);
//
//        assertNotNull(getFutureSpan.getEndPoint());
//        assertNull(frontCacheGetFutureSpan.getEndPoint());
//        assertThat(frontCacheGetFutureSpan.getDestinationId(), is("front"));
    }
}

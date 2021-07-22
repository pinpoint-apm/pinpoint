package com.navercorp.pinpoint.web.calltree.span;

import com.navercorp.pinpoint.common.server.bo.SpanBo;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.function.Predicate;

public class SpanFiltersTest {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private static final long collectorAcceptTime = 1000;
    private static final String agentId = "agent";
    private static final long spanId = 2000;

    @Test
    public void agentIfFilter_check_null() {

        Predicate<SpanBo> filter = SpanFilters.agentIdFilter(agentId);
        SpanBo spanBo = new SpanBo();
        Assert.assertFalse(filter.test(spanBo));
    }

    @Test
    public void buildViewPointFilter() {

        Predicate<SpanBo> filter = SpanFilters.spanFilter(spanId, agentId, collectorAcceptTime);
        SpanBo spanBo = new SpanBo();
        spanBo.setCollectorAcceptTime(collectorAcceptTime);
        spanBo.setAgentId(agentId);
        spanBo.setSpanId(spanId);
        Assert.assertTrue(filter.test(spanBo));
    }

    @Test
    public void buildViewPointFilter_empty_spanId() {
        Predicate<SpanBo> filter = SpanFilters.spanFilter(-1, agentId, collectorAcceptTime);
        SpanBo spanBo = new SpanBo();
        spanBo.setCollectorAcceptTime(collectorAcceptTime);
        spanBo.setAgentId(agentId);
        spanBo.setSpanId(1234);
        Assert.assertTrue(filter.test(spanBo));
    }

    @Ignore
    @Test
    public void buildViewPointFilter_chain_name() {
        Predicate<SpanBo> filter = SpanFilters.spanFilter(-1, agentId, collectorAcceptTime);
        logger.info("filter name:{}", filter);

        Predicate<SpanBo> agentIdFilter = SpanFilters.agentIdFilter("agent1234");
        logger.info("filter name:{}", agentIdFilter);
    }
}
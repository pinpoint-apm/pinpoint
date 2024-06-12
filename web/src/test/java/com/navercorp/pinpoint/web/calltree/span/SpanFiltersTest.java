package com.navercorp.pinpoint.web.calltree.span;

import com.navercorp.pinpoint.common.id.AgentId;
import com.navercorp.pinpoint.common.server.bo.SpanBo;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.util.function.Predicate;

public class SpanFiltersTest {

    private final Logger logger = LogManager.getLogger(this.getClass());

    private static final long collectorAcceptTime = 1000;
    private static final String agentId = "agent";
    private static final long spanId = 2000;

    @Test
    public void agentIfFilter_check_null() {

        Predicate<SpanBo> filter = SpanFilters.agentIdFilter(agentId);
        SpanBo spanBo = new SpanBo();
        Assertions.assertFalse(filter.test(spanBo));
    }

    @Test
    public void buildViewPointFilter() {

        Predicate<SpanBo> filter = SpanFilters.spanFilter(spanId, agentId, collectorAcceptTime);
        SpanBo spanBo = new SpanBo();
        spanBo.setCollectorAcceptTime(collectorAcceptTime);
        spanBo.setAgentId(AgentId.of(agentId));
        spanBo.setSpanId(spanId);
        Assertions.assertTrue(filter.test(spanBo));
    }

    @Test
    public void buildViewPointFilter_empty_spanId() {
        Predicate<SpanBo> filter = SpanFilters.spanFilter(-1, agentId, collectorAcceptTime);
        SpanBo spanBo = new SpanBo();
        spanBo.setCollectorAcceptTime(collectorAcceptTime);
        spanBo.setAgentId(AgentId.of(agentId));
        spanBo.setSpanId(1234);
        Assertions.assertTrue(filter.test(spanBo));
    }

    @Disabled
    @Test
    public void buildViewPointFilter_chain_name() {
        Predicate<SpanBo> filter = SpanFilters.spanFilter(-1, agentId, collectorAcceptTime);
        logger.info("filter name:{}", filter);

        Predicate<SpanBo> agentIdFilter = SpanFilters.agentIdFilter("agent1234");
        logger.info("filter name:{}", agentIdFilter);
    }
}
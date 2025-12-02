/*
 * Copyright 2025 NAVER Corp.
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

package com.navercorp.pinpoint.web.trace.span;

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
        spanBo.setAgentId(agentId);
        spanBo.setSpanId(spanId);
        Assertions.assertTrue(filter.test(spanBo));
    }

    @Test
    public void buildViewPointFilter_empty_spanId() {
        Predicate<SpanBo> filter = SpanFilters.spanFilter(-1, agentId, collectorAcceptTime);
        SpanBo spanBo = new SpanBo();
        spanBo.setCollectorAcceptTime(collectorAcceptTime);
        spanBo.setAgentId(agentId);
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
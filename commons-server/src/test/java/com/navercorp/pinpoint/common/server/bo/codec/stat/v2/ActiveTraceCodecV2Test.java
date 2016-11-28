package com.navercorp.pinpoint.common.server.bo.codec.stat.v2;

import com.navercorp.pinpoint.common.server.bo.codec.stat.AgentStatCodec;
import com.navercorp.pinpoint.common.server.bo.codec.stat.AgentStatCodecTestBase;
import com.navercorp.pinpoint.common.server.bo.codec.stat.TestAgentStatFactory;
import com.navercorp.pinpoint.common.server.bo.stat.ActiveTraceBo;
import com.navercorp.pinpoint.common.trace.SlotType;
import org.junit.Assert;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.Map;

/**
 * @author HyunGil Jeong
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("classpath:applicationContext-test.xml")
public class ActiveTraceCodecV2Test extends AgentStatCodecTestBase<ActiveTraceBo> {

    @Autowired
    private ActiveTraceCodecV2 activeTraceCodecV2;

    @Override
    protected List<ActiveTraceBo> createAgentStats(String agentId, long startTimestamp, long initialTimestamp) {
        return TestAgentStatFactory.createActiveTraceBos(agentId, startTimestamp, initialTimestamp);
    }

    @Override
    protected AgentStatCodec<ActiveTraceBo> getCodec() {
        return activeTraceCodecV2;
    }

    @Override
    protected void verify(ActiveTraceBo expected, ActiveTraceBo actual) {
        Assert.assertEquals("agentId", expected.getAgentId(), actual.getAgentId());
        Assert.assertEquals("startTimestamp", expected.getStartTimestamp(), actual.getStartTimestamp());
        Assert.assertEquals("timestamp", expected.getTimestamp(), actual.getTimestamp());
        Assert.assertEquals("version", expected.getVersion(), actual.getVersion());
        Assert.assertEquals("histogramSchemaType", expected.getHistogramSchemaType(), actual.getHistogramSchemaType());
        if (CollectionUtils.isEmpty(expected.getActiveTraceCounts())) {
            for (Map.Entry<SlotType, Integer> e : actual.getActiveTraceCounts().entrySet()) {
                SlotType slotType = e.getKey();
                int activeTraceCount = e.getValue();
                Assert.assertEquals("activeTraceCount [" + slotType + "]", ActiveTraceBo.UNCOLLECTED_ACTIVE_TRACE_COUNT, activeTraceCount);
            }
        } else {
            Assert.assertEquals("activeTraceCounts", expected.getActiveTraceCounts(), actual.getActiveTraceCounts());
        }
    }
}

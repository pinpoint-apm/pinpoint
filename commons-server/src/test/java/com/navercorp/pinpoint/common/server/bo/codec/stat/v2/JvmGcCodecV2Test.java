package com.navercorp.pinpoint.common.server.bo.codec.stat.v2;

import com.navercorp.pinpoint.common.server.bo.codec.stat.AgentStatCodec;
import com.navercorp.pinpoint.common.server.bo.codec.stat.AgentStatCodecTestBase;
import com.navercorp.pinpoint.common.server.bo.codec.stat.TestAgentStatFactory;
import com.navercorp.pinpoint.common.server.bo.stat.JvmGcBo;
import org.junit.Assert;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.List;

/**
 * @author HyunGil Jeong
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("classpath:applicationContext-test.xml")
public class JvmGcCodecV2Test extends AgentStatCodecTestBase<JvmGcBo> {

    @Autowired
    private JvmGcCodecV2 jvmGcCodecV2;

    @Override
    protected List<JvmGcBo> createAgentStats(String agentId, long startTimestamp, long initialTimestamp) {
        return TestAgentStatFactory.createJvmGcBos(agentId, startTimestamp, initialTimestamp);
    }

    @Override
    protected AgentStatCodec<JvmGcBo> getCodec() {
        return jvmGcCodecV2;
    }

    @Override
    protected void verify(JvmGcBo expected, JvmGcBo actual) {
        Assert.assertEquals("agentId", expected.getAgentId(), actual.getAgentId());
        Assert.assertEquals("startTimestamp", expected.getStartTimestamp(), actual.getStartTimestamp());
        Assert.assertEquals("timestamp", expected.getTimestamp(), actual.getTimestamp());
        Assert.assertEquals("gcType", expected.getGcType(), actual.getGcType());
        Assert.assertEquals("heapUsed", expected.getHeapUsed(), actual.getHeapUsed());
        Assert.assertEquals("heapMax", expected.getHeapMax(), actual.getHeapMax());
        Assert.assertEquals("nonHeapUsed", expected.getNonHeapUsed(), actual.getNonHeapUsed());
        Assert.assertEquals("nonHeapMax", expected.getNonHeapMax(), actual.getNonHeapMax());
        Assert.assertEquals("gcOldCount", expected.getGcOldCount(), actual.getGcOldCount());
        Assert.assertEquals("gcOldTime", expected.getGcOldTime(), actual.getGcOldTime());
    }
}

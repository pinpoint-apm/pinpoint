package com.navercorp.pinpoint.common.server.bo.codec.stat.v2;

import com.navercorp.pinpoint.common.server.bo.codec.stat.AgentStatCodec;
import com.navercorp.pinpoint.common.server.bo.codec.stat.AgentStatCodecTestBase;
import com.navercorp.pinpoint.common.server.bo.codec.stat.TestAgentStatFactory;
import com.navercorp.pinpoint.common.server.bo.serializer.stat.AgentStatUtils;
import com.navercorp.pinpoint.common.server.bo.stat.CpuLoadBo;
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
public class CpuLoadCodecV2Test extends AgentStatCodecTestBase<CpuLoadBo> {

    private static final double DOUBLE_COMPARISON_DELTA = (double) 1 / AgentStatUtils.CONVERT_VALUE;

    @Autowired
    private CpuLoadCodecV2 cpuLoadCodecV2;

    @Override
    protected List<CpuLoadBo> createAgentStats(String agentId, long startTimestamp, long initialTimestamp) {
        return TestAgentStatFactory.createCpuLoadBos(agentId, startTimestamp, initialTimestamp);
    }

    @Override
    protected AgentStatCodec<CpuLoadBo> getCodec() {
        return cpuLoadCodecV2;
    }

    @Override
    protected void verify(CpuLoadBo expected, CpuLoadBo actual) {
        Assert.assertEquals("agentId", expected.getAgentId(), actual.getAgentId());
        Assert.assertEquals("startTimestamp", expected.getStartTimestamp(), actual.getStartTimestamp());
        Assert.assertEquals("timestamp", expected.getTimestamp(), actual.getTimestamp());
        Assert.assertEquals("agentStatType", expected.getAgentStatType(), actual.getAgentStatType());
        Assert.assertEquals("jvmCpuLoad", expected.getJvmCpuLoad(), actual.getJvmCpuLoad(), DOUBLE_COMPARISON_DELTA);
        Assert.assertEquals("systemCpuLoad", expected.getSystemCpuLoad(), actual.getSystemCpuLoad(), DOUBLE_COMPARISON_DELTA);
    }
}

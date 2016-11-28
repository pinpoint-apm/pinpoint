package com.navercorp.pinpoint.common.server.bo.codec.stat;

import com.navercorp.pinpoint.common.buffer.AutomaticBuffer;
import com.navercorp.pinpoint.common.buffer.Buffer;
import com.navercorp.pinpoint.common.buffer.FixedBuffer;
import com.navercorp.pinpoint.common.server.bo.serializer.stat.AgentStatDecodingContext;
import com.navercorp.pinpoint.common.server.bo.serializer.stat.AgentStatUtils;
import com.navercorp.pinpoint.common.server.bo.stat.AgentStatDataPoint;
import org.junit.Assert;
import org.junit.Test;

import java.util.List;

/**
 * @author HyunGil Jeong
 */
public abstract class AgentStatCodecTestBase<T extends AgentStatDataPoint> {

    private static final String AGENT_ID = "testAgentId";
    private static final long AGENT_START_TIMESTAMP = System.currentTimeMillis();
    private static final int NUM_TEST_RUNS = 20;

    protected abstract List<T> createAgentStats(String agentId, long startTimestamp, long initialTimestamp);

    protected abstract AgentStatCodec<T> getCodec();

    protected abstract void verify(T expected, T actual);

    @Test
    public void should_be_encoded_and_decoded_to_same_value() {
        for (int i = 0; i < NUM_TEST_RUNS; ++i) {
            runTest();
        }
    }

    private void runTest() {
        // Given
        final long initialTimestamp = System.currentTimeMillis();
        final long baseTimestamp = AgentStatUtils.getBaseTimestamp(initialTimestamp);
        final long timestampDelta = initialTimestamp - baseTimestamp;
        final List<T> expectedAgentStats = createAgentStats(AGENT_ID, AGENT_START_TIMESTAMP, initialTimestamp);
        // When
        Buffer encodedValueBuffer = new AutomaticBuffer();
        getCodec().encodeValues(encodedValueBuffer, expectedAgentStats);
        // Then
        AgentStatDecodingContext decodingContext = new AgentStatDecodingContext();
        decodingContext.setAgentId(AGENT_ID);
        decodingContext.setBaseTimestamp(baseTimestamp);
        decodingContext.setTimestampDelta(timestampDelta);

        Buffer valueBuffer = new FixedBuffer(encodedValueBuffer.getBuffer());
        List<T> actualAgentStats = getCodec().decodeValues(valueBuffer, decodingContext);
        Assert.assertEquals(expectedAgentStats.size(), actualAgentStats.size());
        for (int i = 0; i < expectedAgentStats.size(); ++i) {
            T expectedAgentStat = expectedAgentStats.get(i);
            T actualAgentStat = actualAgentStats.get(i);
            verify(expectedAgentStat, actualAgentStat);
        }
    }
}

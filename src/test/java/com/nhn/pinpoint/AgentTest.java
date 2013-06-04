package com.nhn.pinpoint;

import com.nhn.pinpoint.profiler.DefaultAgent;
import com.nhn.pinpoint.profiler.config.ProfilerConfig;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 *
 */
public class AgentTest {

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Test
    public void testGetIdentifier() throws Exception {
        checkIdentifier();
        checkIdentifier();
        checkIdentifier();
        checkIdentifier();
    }

    private void checkIdentifier() throws IOException {
        ProfilerConfig config = new ProfilerConfig();
//        config.readConfigFile();
        DefaultAgent agent = new DefaultAgent("", new DummyInstrumentation(), config);

        short identifier = agent.getIdentifier();
        logger.info("{}", identifier);
    }
}

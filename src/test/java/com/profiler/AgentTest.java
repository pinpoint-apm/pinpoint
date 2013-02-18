package com.profiler;

import com.profiler.config.ProfilerConfig;
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
        config.readConfigFile();
        Agent agent = new Agent(config);

        short identifier = agent.getIdentifier();
        logger.info("{}", identifier);
    }
}

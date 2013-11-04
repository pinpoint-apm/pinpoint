package com.nhn.pinpoint.profiler;

import com.nhn.pinpoint.profiler.config.ProfilerConfig;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * @author emeroad
 */
public class AgentTest {

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Test
    public void testGetIdentifier() throws Exception {
        // identifier가 pid기반으로 변경되어 테스트 삭제.
    }

}

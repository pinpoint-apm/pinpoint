package com.navercorp.pinpoint.profiler.context;

import com.navercorp.pinpoint.bootstrap.context.AsyncState;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * @author Woonduk Kang(emeroad)
 */
public class DisableAsyncState implements AsyncState {
    private final Logger logger = LogManager.getLogger(this.getClass());

    @Override
    public void setup() {
        logger.debug("setup");
    }

    @Override
    public boolean await() {
        logger.debug("await");
        return false;
    }

    @Override
    public void finish() {
        logger.debug("finish");
    }
}

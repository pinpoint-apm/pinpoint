package com.nhn.pinpoint.profiler.monitor.metric;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author emeroad
 */
public class DefaultAcceptHistogram implements AcceptHistogram {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final AcceptHistogram staticAcceptHistogram = new StaticAcceptHistogram();

    private final AcceptHistogram dynamicAcceptHistogram = new DynamicAcceptHistogram();

    @Override
    public boolean addResponseTime(String parentApplicationName, short serviceType, int millis) {

        final boolean staticResult = this.staticAcceptHistogram.addResponseTime(parentApplicationName, serviceType, millis);
        if (staticResult) {
            return true;
        }

        final boolean dynamicResult = this.dynamicAcceptHistogram.addResponseTime(parentApplicationName, serviceType, millis);
        if (!dynamicResult) {
            logger.info("response data add fail. parentApplicationName:{} serviceType:{} millis:{}", parentApplicationName, serviceType, millis);
            return true;
        }
        return false;
    }
}

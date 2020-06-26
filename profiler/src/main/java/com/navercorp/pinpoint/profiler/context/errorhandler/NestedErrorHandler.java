package com.navercorp.pinpoint.profiler.context.errorhandler;

import com.navercorp.pinpoint.common.util.Assert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NestedErrorHandler implements IgnoreErrorHandler {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final IgnoreErrorHandler errorHandler;
    // Depence infinite cycle
//    private final int maxDepth;

    public NestedErrorHandler(IgnoreErrorHandler errorHandler) {
        this.errorHandler = Assert.requireNonNull(errorHandler, "errorHandler");
    }

    @Override
    public boolean handleError(Throwable th) {
//        int depth = 0;
        while (th != null) {
            if (this.errorHandler.handleError(th)) {
                return true;
            }
            th = th.getCause();

//            depth++;
//            if (depth > maxDepth) {
//                // Infinite loop in th.toString()
//                logger.warn("Nested depth overflow depth:{} th:", depth, th);
//                return false;
//            }
        }
        return false;
    }

    @Override
    public String toString() {
        return "NestedErrorHandler{" +
                "errorHandler=" + errorHandler +
                '}';
    }
}

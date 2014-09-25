package com.nhn.pinpoint.web.service;

import com.nhn.pinpoint.web.vo.Application;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.Set;

/**
 * @author emeroad
 */
public class LinkVisitChecker {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final Set<Application> calleeFound = new HashSet<Application>();
    private final Set<Application> callerFound = new HashSet<Application>();

    public boolean visitCaller(Application caller) {
        if (caller == null) {
            throw new NullPointerException("caller must not be null");
        }
        final boolean found = !callerFound.add(caller);
        if (logger.isDebugEnabled()) {
            if (found) {
                logger.debug("Finding Caller. caller={}", caller);
            } else {
                logger.debug("LinkData exists. Skip finding caller. {} ", caller);
            }
        }
        return found;
    }

    public boolean visitCallee(Application callee) {
        if (callee == null) {
            throw new NullPointerException("callee must not be null");
        }
        final boolean found = !this.calleeFound.add(callee);
        if (logger.isDebugEnabled()) {
            if (found) {
                logger.debug("Finding Callee. callee={}", callee);
            } else {
                logger.debug("LinkData exists. Skip finding callee. {} ", callee);
            }
        }
        return found;
    }
}

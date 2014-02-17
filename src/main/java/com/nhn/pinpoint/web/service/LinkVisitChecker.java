package com.nhn.pinpoint.web.service;

import com.nhn.pinpoint.web.applicationmap.rawdata.LinkStatistics;
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
        final Set<Application> callerFound = this.callerFound;
        if (callerFound.contains(caller)) {
            logger.debug("LinkStatistics exists. Skip finding caller. {} ", caller);
            return true;
        }
        callerFound.add(caller);
        if (logger.isDebugEnabled()) {
            logger.debug("Finding Caller. caller={}", caller);
        }
        return false;
    }

    public boolean visitCallee(Application callee) {
        final Set<Application> calleeFound = this.calleeFound;
        if (calleeFound.contains(callee)) {
            logger.debug("LinkStatistics exists. Skip finding callee. {} ", callee);
            return true;
        }
        calleeFound.add(callee);
        if (logger.isDebugEnabled()) {
            logger.debug("Finding Callee. callee={}", callee);
        }
        return false;
    }
}

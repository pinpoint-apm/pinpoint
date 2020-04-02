/*
 * Copyright 2017 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.navercorp.pinpoint.web.service.map;

import com.google.common.collect.Sets;
import com.navercorp.pinpoint.web.service.SearchDepth;
import com.navercorp.pinpoint.web.vo.Application;
import com.navercorp.pinpoint.web.vo.Range;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * @author HyunGil Jeong
 */
public class LinkSelectContext {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final Range range;
    private final SearchDepth callerDepth;
    private final SearchDepth calleeDepth;
    private final LinkVisitChecker linkVisitChecker;

    private final Set<Application> nextApplications = Sets.newConcurrentHashSet();

    public LinkSelectContext(Range range, SearchDepth callerDepth, SearchDepth calleeDepth, LinkVisitChecker linkVisitChecker) {
        this.range = Objects.requireNonNull(range, "range");
        this.callerDepth = Objects.requireNonNull(callerDepth, "callerDepth");
        this.calleeDepth = Objects.requireNonNull(calleeDepth, "calleeDepth");
        this.linkVisitChecker = Objects.requireNonNull(linkVisitChecker, "linkVisitChecker");
    }

    public Range getRange() {
        return range;
    }

    public int getCallerDepth() {
        return callerDepth.getDepth();
    }

    public int getCalleeDepth() {
        return calleeDepth.getDepth();
    }

    public boolean checkNextCaller(Application application) {
        if (callerDepth.isDepthOverflow()) {
            logger.debug("caller depth overflow application:{} depth:{}", application, callerDepth.getDepth());
            return false;
        }
        if (linkVisitChecker.visitCaller(application)) {
            logger.debug("already visited caller:{}", application);
            return false;
        }
        return true;
    }

    public boolean checkNextCallee(Application application) {
        if (calleeDepth.isDepthOverflow()) {
            logger.debug("callee depth overflow application:{} depth:{}", application, calleeDepth.getDepth());
            return false;
        }
        if (linkVisitChecker.visitCallee(application)) {
            logger.debug("already visited callee:{}", application);
            return false;
        }
        return true;
    }

    public List<Application> getNextApplications() {
        List<Application> nextApplications = new ArrayList<>(this.nextApplications);
        return nextApplications;
    }

    public void addNextApplication(Application application) {
        final boolean add = this.nextApplications.add(application);
        if (!add) {
            logger.debug("already added. nextNode:{}", application);
        }
    }

    public LinkSelectContext advance() {
        SearchDepth nextCallerDepth = callerDepth.nextDepth();
        SearchDepth nextCalleeDepth = calleeDepth.nextDepth();
        LinkSelectContext nextContext = new LinkSelectContext(range, nextCallerDepth, nextCalleeDepth, linkVisitChecker);
        return nextContext;
    }
}

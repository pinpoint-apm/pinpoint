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
import com.navercorp.pinpoint.web.vo.Application;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Set;

/**
 * @author emeroad
 * @author HyunGil Jeong
 */
public class LinkVisitChecker {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final Set<Application> calleeFound = Sets.newConcurrentHashSet();
    private final Set<Application> callerFound = Sets.newConcurrentHashSet();

    public boolean visitCaller(Application caller) {
        return visit(callerFound, caller, "Caller");
    }

    public boolean isVisitedCaller(Application caller) {
        return callerFound.contains(caller);
    }

    public boolean visitCallee(Application callee) {
        return visit(calleeFound, callee, "Callee");
    }

    private boolean visit(Set<Application> visitedSet, Application application, String type) {
        if (application == null) {
            throw new NullPointerException("application must not be null");
        }
        final boolean alreadyVisited = !visitedSet.add(application);
        if (logger.isDebugEnabled()) {
            if (alreadyVisited) {
                logger.debug("LinkData exists. Skip finding {}. {} ", type, application);
            }
        }
        return alreadyVisited;
    }
}

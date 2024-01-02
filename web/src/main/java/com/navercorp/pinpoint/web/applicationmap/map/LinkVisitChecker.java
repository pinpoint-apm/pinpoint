/*
 * Copyright 2023 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package com.navercorp.pinpoint.web.applicationmap.map;

import com.navercorp.pinpoint.web.applicationmap.link.LinkDirection;
import com.navercorp.pinpoint.web.vo.Application;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author emeroad
 * @author HyunGil Jeong
 */
public class LinkVisitChecker {

    private final Logger logger = LogManager.getLogger(this.getClass());

    private final Set<Application> outFound = ConcurrentHashMap.newKeySet();
    private final Set<Application> inFound = ConcurrentHashMap.newKeySet();

    public boolean visitOut(Application out) {
        return visit(outFound, out, LinkDirection.OUT_LINK);
    }

    public boolean isVisitedOut(Application in) {
        return outFound.contains(in);
    }

    public boolean visitIn(Application in) {
        return visit(inFound, in, LinkDirection.IN_LINK);
    }

    private boolean visit(Set<Application> visitedSet, Application application, LinkDirection type) {
        Objects.requireNonNull(visitedSet, "application");

        final boolean alreadyVisited = !visitedSet.add(application);
        if (alreadyVisited) {
            if (logger.isDebugEnabled()) {
                logger.debug("LinkData exists. Skip finding {}. {} ", type, application);
            }
        }
        return alreadyVisited;
    }
}

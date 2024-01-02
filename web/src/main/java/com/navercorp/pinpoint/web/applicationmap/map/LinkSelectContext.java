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

import com.navercorp.pinpoint.common.server.util.time.Range;
import com.navercorp.pinpoint.web.applicationmap.link.LinkDirection;
import com.navercorp.pinpoint.web.applicationmap.service.SearchDepth;
import com.navercorp.pinpoint.web.vo.Application;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;

/**
 * @author HyunGil Jeong
 */
public class LinkSelectContext {

    private final Logger logger = LogManager.getLogger(this.getClass());

    private final Range range;
    private final SearchDepth outDepth;
    private final SearchDepth inDepth;

    private final LinkVisitChecker linkVisitChecker;
    private final boolean timeAggregated;

    private final Set<Application> nextApplications = ConcurrentHashMap.newKeySet();

    public LinkSelectContext(Range range, SearchDepth outDepth, SearchDepth inDepth,
                             LinkVisitChecker linkVisitChecker, boolean timeAggregated) {
        this.range = Objects.requireNonNull(range, "range");
        this.outDepth = Objects.requireNonNull(outDepth, "outDepth");
        this.inDepth = Objects.requireNonNull(inDepth, "inDepth");
        this.linkVisitChecker = Objects.requireNonNull(linkVisitChecker, "linkVisitChecker");
        this.timeAggregated = timeAggregated;
    }

    public Range getRange() {
        return range;
    }

    public int getOutDepth() {
        return outDepth.getDepth();
    }

    public int getInDepth() {
        return inDepth.getDepth();
    }

    public boolean isTimeAggregated() {
        return timeAggregated;
    }

    public boolean checkNextOut(Application application) {
        return filterNextLink(LinkDirection.OUT_LINK, outDepth, application, linkVisitChecker::visitOut);
    }

    public boolean checkNextIn(Application application) {
        return filterNextLink(LinkDirection.IN_LINK, inDepth, application, linkVisitChecker::visitIn);
    }

    private boolean filterNextLink(LinkDirection inLink, SearchDepth inDepth, Application application, Predicate<Application> linkVisitChecker) {
        if (inDepth.isDepthOverflow()) {
            logger.debug("{} depth overflow application:{} depth:{}", inLink, application, inDepth.getDepth());
            return false;
        }
        if (linkVisitChecker.test(application)) {
            logger.debug("already visited {}:{}", inLink, application);
            return false;
        }
        return true;
    }

    public List<Application> getNextApplications() {
        return new ArrayList<>(this.nextApplications);
    }

    public void addNextApplication(Application application) {
        final boolean add = this.nextApplications.add(application);
        if (!add) {
            logger.debug("already added. nextNode:{}", application);
        }
    }

    public LinkSelectContext advance() {
        SearchDepth nextOutDepth = outDepth.nextDepth();
        SearchDepth nextInDepth = inDepth.nextDepth();
        return new LinkSelectContext(range, nextOutDepth, nextInDepth, linkVisitChecker, timeAggregated);
    }
}

/*
 * Copyright 2014 NAVER Corp.
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

package com.navercorp.pinpoint.web.calltree.span;

import java.util.*;

import com.navercorp.pinpoint.common.bo.SpanBo;
import com.navercorp.pinpoint.common.bo.SpanEventBo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author netspider
 * @author emeroad
 * @author jaehong.kim
 */
public class SpanAligner2 {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    // not matched
    public static final int FAIL_MATCH = 0;
    // transaction completed succesfully
    public static final int BEST_MATCH = 1;
    // transaction in-flight or missing data
    public static final int START_TIME_MATCH = 2;

    private static final Long ROOT = -1L;
    private static final int PARENT_DEPTH = -1;
    private final Map<Long, List<SpanBo>> spanIdMap;
    private Long rootSpanId = null;
    private int matchType = FAIL_MATCH;

    public SpanAligner2(List<SpanBo> spans, long collectorAcceptTime) {
        this.spanIdMap = buildSpanMap(spans);
        this.rootSpanId = findRootSpanId(spans, collectorAcceptTime);
    }

    private long findRootSpanId(List<SpanBo> spanList, long collectorAcceptTime) {
        if (spanList == null) {
            throw new NullPointerException("spanList must not be null");
        }

        final List<SpanBo> root = new ArrayList<SpanBo>();
        for (SpanBo span : spanList) {
            if (span.getParentSpanId() == ROOT) {
                root.add(span);
            }
        }
        // perfect match condition
        final int rootSpanBoSize = root.size();
        if (rootSpanBoSize == 1) {
            final SpanBo spanBo = root.get(0);
            logger.debug("root span found. best match:{}", spanBo);
            matchType = BEST_MATCH;
            // XXX in case where root exist but no span queried. additional logic needed
            return spanBo.getSpanId();
        }
        // XXX: a bug in the logic if rootspan is more than 2. should we display randomly?
        if (rootSpanBoSize > 1) {
            logger.warn("parentSpanId(-1) collision. size:{} root span:{} allSpan:{}", rootSpanBoSize, root, spanList);
            throw new IllegalStateException("parentSpanId(-1) collision. size:" + rootSpanBoSize);
        }

        // missing root or incomplete root (not arrived yet): meaning on-going process
        // next best thing is to lookup span based on the beginning of time of span it looked up
        // most likely data exist since the data gets extracted from span. non-existent data possible due to hbase insertion failure
        final List<SpanBo> collectorAcceptTimeMatcher = new ArrayList<SpanBo>();
        for (SpanBo span : spanList) {
            // collectorTime is a hint
            if (span.getCollectorAcceptTime() == collectorAcceptTime) {
                collectorAcceptTimeMatcher.add(span);
            }
        }
        // a match based on startTime. a more accurate matching is possible when additional information is given (see below)
        // which one of these leads to a best match? probably agentId.
        // "applicationName" : "/httpclient4/post.pinpoint",
        // "transactionId" : "emeroad-pc^1382955966412^16",
        // "agentId" : "emeroad-pc",
        // "applicationId" : "emeroad-app",
        // "callStackStart" : 1383024213315,
        // "callStackEnd" : 2010,
        final int startMatchSize = collectorAcceptTimeMatcher.size();
        if (startMatchSize == 1) {
            final SpanBo spanBo = collectorAcceptTimeMatcher.get(0);
            logger.info("collectorAcceptTime span found startTime match:{}", spanBo);
            matchType = START_TIME_MATCH;
            return spanBo.getSpanId();
        }
        if (startMatchSize > 1) {
            logger.warn("collectorAcceptTime match collision. size:{} collectorAcceptTime:{} allSpan:{}", startMatchSize, collectorAcceptTime, spanList);
            throw new IllegalStateException("startTime match collision size:" + startMatchSize + " collectorAcceptTime:" + collectorAcceptTime);
        }
        // can we do better? There doesn't seem to be a definitive answer for rendering the call stack
        logger.warn("collectorAcceptTime match not found. size:{} collectorAcceptTime:{} allSpan:{}", startMatchSize, collectorAcceptTime, spanList);
        throw new IllegalStateException("startTime match not found startTime size:" + startMatchSize + " collectorAcceptTime:" + collectorAcceptTime);
    }

    private Map<Long, List<SpanBo>> buildSpanMap(List<SpanBo> spans) {
        final Map<Long, List<SpanBo>> spanMap = new HashMap<Long, List<SpanBo>>();
        for (SpanBo span : spans) {
            final long spanId = span.getSpanId();
            List<SpanBo> spanBoList = spanMap.get(spanId);
            if (spanBoList == null) {
                spanBoList = new ArrayList<SpanBo>();
                spanBoList.add(span);
                spanMap.put(spanId, spanBoList);
            } else {
                spanBoList.add(span);
            }
        }
        return spanMap;
    }

    public List<SpanAlign> sort() {
        final List<SpanBo> rootList = spanIdMap.remove(rootSpanId);
        if (rootList == null || rootList.size() == 0) {
            throw new IllegalStateException("rootList span not found. rootSpanId=" + rootSpanId + ", map=" + spanIdMap.keySet());
        }
        if (rootList.size() > 1) {
            throw new IllegalStateException("duplicate rootList span found. rootSpanId=" + rootSpanId + ", map=" + spanIdMap.keySet());
        }
        SpanBo rootSpanBo = rootList.get(0);
        final List<SpanAlign> list = new ArrayList<SpanAlign>();

        populate(rootSpanBo, 0, list);

        return list;
    }

    public int getMatchType() {
        return matchType;
    }

    private void populate(SpanBo span, int spanDepth, List<SpanAlign> container) {
        logger.debug("populate start");
        SpanAlignDepth spanAlignDepth = new SpanAlignDepth(spanDepth);
        SpanAlign spanAlign = new SpanAlign(spanDepth, span);
        container.add(spanAlign);

        AsyncSpanEventAligner aligner = new AsyncSpanEventAligner();
        List<SpanEventBo> spanEventBoList = aligner.sort(span.getSpanEventBoList());
        if (spanEventBoList == null) {
            return;
        }
        if (logger.isDebugEnabled()) {
            for (SpanEventBo spanEvent : spanEventBoList) {
                logger.debug("Align span event {}", spanEvent);
            }
        }

        spanAlign.setHasChild(true);

        boolean asyncEventMissing = false;
        for (SpanEventBo spanEventBo : spanEventBoList) {
            if (spanEventBo.isAsync() && asyncEventMissing) {
                continue;
            }

            if (spanAlignDepth.isParentMissing(spanEventBo)) {
                logger.debug("Parent missing and make missingEvent");

                final int currentDepth = spanAlignDepth.getMissingDepth(spanEventBo);
                MissedSpanAlignFactory factory = new MissedSpanAlignFactory();
                final SpanAlign spanEventAlign = factory.get(currentDepth, span, spanEventBo);
                container.add(spanEventAlign);

                if (spanEventBo.isAsync()) {
                    asyncEventMissing = true;
                    continue;
                } else {
                    break;
                }
            }

            final int currentDepth = spanAlignDepth.getDepth(spanEventBo);
            final SpanAlign spanEventAlign = new SpanAlign(currentDepth, span, spanEventBo);
            container.add(spanEventAlign);

            if (logger.isDebugEnabled()) {
                logger.debug("spanEvent type:{} depth:{} spanEventDepth:{} ", spanEventBo.getServiceType(), currentDepth, spanEventBo.getDepth());
            }

            final long nextSpanId = spanEventBo.getNextSpanId();
            final List<SpanBo> nextSpanBoList = spanIdMap.remove(nextSpanId);
            if (nextSpanId != ROOT && nextSpanBoList != null) {
                int childDepth = currentDepth + 1;

                SpanBo spanBo = getNextSpan(span, spanEventBo, nextSpanBoList);
                if (spanBo != null) {
                    populate(spanBo, childDepth, container);
                } else {
                    // TODO add missed
                    logger.debug("nextSpanId not found. {}", nextSpanId);
                }
            }
        }
        logger.debug("populate end");
    }

    // fix nextSpan collision problem
    private SpanBo getNextSpan(SpanBo span, SpanEventBo beforeSpanEventBo, List<SpanBo> nextSpanBoList) {
        if (logger.isDebugEnabled()) {
            logger.debug("beforeSpanEvent:{}, nextSpanBoList:{}", beforeSpanEventBo, nextSpanBoList);
        }
        if (nextSpanBoList.size() == 1) {
            return nextSpanBoList.get(0);
        } else if (nextSpanBoList.size() > 1) {
            // attempt matching based on similarity
            // return spanBos.get(0);
            long spanEventBoStartTime = span.getStartTime() + beforeSpanEventBo.getStartElapsed();

            SpanIdMatcher spanIdMatcher = new SpanIdMatcher(nextSpanBoList);
            // very susceptible to things like packet loss due to similarilty match based on restricted set of data
            // TODO: need to find a better way to calc similarity based on entire data
            SpanBo matched = spanIdMatcher.approximateMatch(spanEventBoStartTime);
            if (matched == null) {
                // no matching span
                return null;
            }
            List<SpanBo> other = spanIdMatcher.other();
            if (other != null) {
                spanIdMap.put(matched.getSpanId(), other);
            }
            return matched;
        } else {
            throw new IllegalStateException("error");
        }
    }
}

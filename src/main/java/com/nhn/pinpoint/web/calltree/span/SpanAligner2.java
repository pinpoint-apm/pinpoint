package com.nhn.pinpoint.web.calltree.span;

import java.util.*;

import com.nhn.pinpoint.common.bo.SpanBo;
import com.nhn.pinpoint.common.bo.SpanEventBo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * @author netspider
 * @author emeroad
 */
public class SpanAligner2 {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    // 매치가 안됨.
    public static final int FAIL_MATCH = 0;
    // transaction이 완벽하게 끝남.
    public static final int BEST_MATCH = 1;
    // transaction이 진행중이거나. 일부 분실된 데이터가 있음.
    public static final int START_TIME_MATCH = 2;


    private static final Long ROOT = -1L;
	private final Map<Long, List<SpanBo>> spanIdMap;
	private Long rootSpanId = null;
    private int matchType = FAIL_MATCH;

	public SpanAligner2(List<SpanBo> spans, long collectorAcceptTime) {
        this.spanIdMap = buildSpanMap(spans);
        this.rootSpanId = findRootSpanId(spans, collectorAcceptTime);
    }

    private long findRootSpanId(List<SpanBo> spans, long collectorAcceptTime) {
        final List<SpanBo> root = new ArrayList<SpanBo>();
        for (SpanBo span : spans) {
            if (span.getParentSpanId() == ROOT) {
                root.add(span);
            }
        }
        // 최상 조건의 best매치. 완벽 조건의 매치.
        final int rootSpanBoSize = root.size();
        if (rootSpanBoSize == 1) {
            final SpanBo spanBo = root.get(0);
            logger.debug("root span found. best match:{}", spanBo);
            matchType = BEST_MATCH;
            // 틈세가 추가로 있음. root는 있으나 조회한 span이 없을 경우 추가처리가 있어야함.
            return spanBo.getSpanId();
        }
        // 버그 rootspan이 2개 이상인 경우는 로직 버그이다. 아무거나 잡아서 데이터를 뿌려줘야 되나?
        if (rootSpanBoSize > 1) {
            logger.warn("parentSpanId(-1) collision. size:{} root span:{} allSpan:{}", rootSpanBoSize, root, spans);
            throw new IllegalStateException("parentSpanId(-1) collision. size:" + rootSpanBoSize);
        }

        // root 분실. 혹은 아직 도착하지 않아 root가 완성 되지 않음. 즉 진행중인 process일 수 있음.
        // 차선책으로 자신이 조회한 span의 시작 시간을 기준으로 span을 조회한다.
        // span에서 데이터를 추출하는 것이기 때문에, 왠간하면 데이터는 존재함. hbase insert시 data insert를 실패할 경우 없을수 있음.
        final List<SpanBo> collectorAcceptTimeMatcher = new ArrayList<SpanBo>();
        for(SpanBo span : spans) {
            // collectorTime이 힌트로 들어온다.
            if (span.getCollectorAcceptTime() == collectorAcceptTime) {
                collectorAcceptTimeMatcher.add(span);
            }
        }
        // startTime 기반 match. 아래 추가 정보가 제공 되면 더 정확하게 매치가 가능하다.
        // 이중에서 어느 정보를 얻으면 가장 쉽고 정확하게 매치가 가능한가? agentId가 제일 무난하지 않나 함.
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
            logger.warn("collectorAcceptTime match collision. size:{} collectorAcceptTime:{} allSpan:{}", startMatchSize, collectorAcceptTime, spans);
            throw new IllegalStateException("startTime match collision size:" + startMatchSize + " collectorAcceptTime:" + collectorAcceptTime);
        }
        // 여기서 다음상황으로 더 정확하게 매치가 가능한가? 마땅히 call stack을 랜더링 할수 있는 방법 없음
        logger.warn("collectorAcceptTime match not found. size:{} collectorAcceptTime:{} allSpan:{}", startMatchSize, collectorAcceptTime, spans);
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
		int currentDepth = spanDepth;
        if (logger.isDebugEnabled()) {
            logger.debug("span type:{} depth:{} spanDepth:{} ", currentDepth, span.getServiceType(), spanDepth);
        }
		
		SpanAlign spanAlign = new SpanAlign(currentDepth, span);
		container.add(spanAlign);

		List<SpanEventBo> spanEventBoList = span.getSpanEventBoList();
        if (spanEventBoList == null) {
            return;
        }
        
        spanAlign.setHasChild(true);
        
		for (SpanEventBo spanEventBo : spanEventBoList) {
			if (spanEventBo. getDepth() != -1) {
				currentDepth = spanDepth + spanEventBo.getDepth();
			}
            if (logger.isDebugEnabled()) {
                logger.debug("spanEvent type:{} depth:{} spanEventDepth:{} ", spanEventBo.getServiceType(), currentDepth, spanEventBo.getDepth());
            }

			SpanAlign spanEventAlign = new SpanAlign(currentDepth, span, spanEventBo);
			container.add(spanEventAlign);

			final long nextSpanId = spanEventBo.getNextSpanId();
            final List<SpanBo> nextSpanBoList = spanIdMap.remove(nextSpanId);
            if (nextSpanId != ROOT && nextSpanBoList != null) {
                int childDepth = currentDepth + 1;

                SpanBo spanBo = getNextSpan(span, spanEventBo, nextSpanBoList);
                if (spanBo != null) {
                    populate(spanBo, childDepth, container);
                } else {
                    logger.debug("nextSpanId not found. {}", nextSpanId);
                }
			}
		}
        logger.debug("populate end");
	}

    // nextSpan의 충돌 까지 해결한다.
    private SpanBo getNextSpan(SpanBo span, SpanEventBo beforeSpanEventBo, List<SpanBo> nextSpanBoList) {
        if (logger.isDebugEnabled()) {
            logger.debug("beforeSpanEvent:{}, nextSpanBoList:{}", beforeSpanEventBo, nextSpanBoList);
        }
        if (nextSpanBoList.size() == 1) {
            return nextSpanBoList.get(0);
        } else if(nextSpanBoList.size() > 1) {
            // 최대한 비슷한 매칭을 시도한다.
//            return spanBos.get(0);
            long spanEventBoStartTime = span.getStartTime() + beforeSpanEventBo.getStartElapsed();

            SpanIdMatcher spanIdMatcher = new SpanIdMatcher(nextSpanBoList);
            // 전체를 보지 않고 일부만 보고 유사도를 측정하므로, 패킷 lost등에 매우 취약함. 전체를 보고 근사도를 추가 분석하는 방법이 강구되어야 될것 같음.
            SpanBo matched = spanIdMatcher.approximateMatch(spanEventBoStartTime);
            if (matched == null) {
                // match되는 span을 찾을수 없음.
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

package com.nhn.pinpoint.web.calltree.span;

import java.util.*;

import com.nhn.pinpoint.common.bo.SpanBo;
import com.nhn.pinpoint.common.bo.SpanEventBo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * @author netspider
 * 
 */
public class SpanAligner2 {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    // 매치가 안됨.
    public static final int FAIL_MATCH = 0;
    // transaction이 완벽하게 끝남.
    public static final int BEST_MATCH = 1;
    // transaction이 진행중이거나. 일부 분실된 데이터가 있음.
    public static final int START_TIME_MATCH = 2;


    private static final Integer ROOT = -1;
	private final Map<Integer, List<SpanBo>> spanMap;
	private Integer rootSpanId = null;
    private int matchType = FAIL_MATCH;

	public SpanAligner2(List<SpanBo> spans, long collectorAcceptTime) {
        this.spanMap = buildSpanMap(spans);
        this.rootSpanId = findRootSpanId(spans, collectorAcceptTime);
    }

    private int findRootSpanId(List<SpanBo> spans, long collectorAcceptTime) {
        final List<SpanBo> root = new ArrayList<SpanBo>();
        for (SpanBo span : spans) {
            if (span.getParentSpanId() == ROOT) {
                root.add(span);
            }
        }
        // 최상 조건의 best매치. 완벽 조건의 매치.
        final int matchSize = root.size();
        if (matchSize == 1) {
            final SpanBo spanBo = root.get(0);
            logger.debug("root span found best match:{}", spanBo);
            matchType = BEST_MATCH;
            return spanBo.getSpanId();
        }
        // 버그 rootspan이 2개 이상인 경우는 로직 버그이다. 아무거나 잡아서 데이터를 뿌려줘야 되나?
        if (matchSize > 1) {
            logger.warn("parentSpanId(-1) collision. size:{} root span:{} allSpan:{}", matchSize, root, spans);
            throw new IllegalStateException("parentSpanId(-1) collision. size:" + matchSize);
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

    private Map<Integer, List<SpanBo>> buildSpanMap(List<SpanBo> spans) {
        final Map<Integer, List<SpanBo>> spanMap = new HashMap<Integer, List<SpanBo>>();
        for (SpanBo span : spans) {
            List<SpanBo> spanBoList = spanMap.get(span.getSpanId());
            if (spanBoList == null) {
                spanBoList = new ArrayList<SpanBo>();
                spanBoList.add(span);
                spanMap.put(span.getSpanId(), spanBoList);
            } else {
                spanBoList.add(span);
            }
        }
        return spanMap;
    }

    public List<SpanAlign> sort() {
		List<SpanAlign> list = new ArrayList<SpanAlign>();
		final List<SpanBo> rootList = spanMap.get(rootSpanId);
		if (rootList == null || rootList.size() == 0) {
			throw new IllegalStateException("rootList span not found. rootSpanId=" + rootSpanId + ", map=" + spanMap.keySet());
		}
        if (rootList.size() > 1) {
            throw new IllegalStateException("duplicate rootList span found. rootSpanId=" + rootSpanId + ", map=" + spanMap.keySet());
        }
        SpanBo rootSpanBo = rootList.get(0);
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

			// TODO spanEvent이 drop되면 container에 채워지지 못하는 Span이 생길 수 있다.
			int nextSpanId = spanEventBo.getNextSpanId();
			if (nextSpanId != ROOT && spanMap.containsKey(nextSpanId)) {
                int childDepth = currentDepth + 1;
                // TODO 중복 처리?
                SpanBo spanBo = spanMap.get(nextSpanId).get(0);
                logger.info("remote spanEvent:{} spanBo:{}", spanEventBo, spanBo);
                if (spanBo != null) {
                    populate(spanBo, childDepth, container);
                } else {
                    logger.info("nextSpanId not found. {}", nextSpanId);
                }
			}
		}
        logger.debug("populate end");
	}
}

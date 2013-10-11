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

    private static final Integer ROOT = -1;
	private final Map<Integer, SpanBo> spanMap;
	private Integer rootSpanId = null;

	public SpanAligner2(List<SpanBo> spans) {
		spanMap = new HashMap<Integer, SpanBo>(spans.size());

		long rootSpanStartTime = Long.MAX_VALUE;

		for (SpanBo span : spans) {
			if (spanMap.containsKey(span.getSpanId())) {
				throw new IllegalStateException("duplicated spanId. id:" + span.getSpanId());
			}

			if (span.getParentSpanId() == ROOT) {
				rootSpanId = ROOT;
				spanMap.put(ROOT, span);
				continue;
			} else if ((rootSpanId == null || !rootSpanId.equals(ROOT)) && span.getStartTime() < rootSpanStartTime) {
				rootSpanId = (span.getParentSpanId() == ROOT) ? ROOT : span.getSpanId();
				rootSpanStartTime = span.getStartTime();
			}
			spanMap.put(span.getSpanId(), span);
		}
	}

	public List<SpanAlign> sort() {
		List<SpanAlign> list = new ArrayList<SpanAlign>();
		final SpanBo root = spanMap.get(rootSpanId);
		if (root == null) {
			throw new IllegalStateException("root span not found. rootSpanId=" + rootSpanId + ", map=" + spanMap.keySet());
		}

		populate(root, 0, list);

		return list;
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
                SpanBo spanBo = spanMap.get(nextSpanId);
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

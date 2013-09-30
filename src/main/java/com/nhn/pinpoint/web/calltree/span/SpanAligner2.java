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
		SpanBo root = spanMap.get(rootSpanId);

		if (root == null) {
			throw new IllegalStateException("root span not found. rootSpanId=" + rootSpanId + ", map=" + spanMap.keySet());
		}

		populate(root, 0, 0, 0, list);

		return list;
	}

	private int populate(SpanBo parentSpan, int spanDepth, int sequence, int pSequence, List<SpanAlign> container) {
		int depth = spanDepth + 1;
        if (logger.isDebugEnabled()) {
            logger.info("span depth:{}", depth);
        }
		int lastChildSequence = sequence;
		
		SpanAlign element = new SpanAlign(depth, parentSpan, ++lastChildSequence, pSequence);
		container.add(element);

		List<SpanEventBo> spanEventBoList = parentSpan.getSpanEventBoList();
        if (spanEventBoList == null) {
            return sequence;
        }
        
        element.setHasChild(true);
        
		for (SpanEventBo spanEventBo : spanEventBoList) {
			if (spanEventBo.getDepth() != -1) {
				depth = spanDepth + spanEventBo.getDepth();
                if (logger.isDebugEnabled()) {
                    logger.debug("spanEvent spanEvent{} depth:{} getDepth():{}", spanEventBo.getServiceType(), depth, spanEventBo.getDepth());
                }
			} else {
                if (logger.isDebugEnabled()) {
                    logger.debug("spanEvent depth:{} ", depth);
                }
            }
			
			lastChildSequence++;
			
			SpanAlign sa = new SpanAlign(depth, lastChildSequence, sequence, parentSpan, spanEventBo);
			container.add(sa);

			// TODO spanEvent이 drop되면 container에 채워지지 못하는 Span이 생길 수 있다.
			int nextSpanId = spanEventBo.getNextSpanId();
			if (nextSpanId != ROOT && spanMap.containsKey(nextSpanId)) {
				lastChildSequence = populate(spanMap.get(nextSpanId), depth, lastChildSequence, lastChildSequence, container);
			}
		}
		
		return lastChildSequence;
	}
}

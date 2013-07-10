package com.nhn.pinpoint.web.calltree.span;

import java.util.*;

import com.nhn.pinpoint.common.bo.SpanBo;
import com.nhn.pinpoint.common.bo.SpanEventBo;

/**
 * 
 * @author netspider
 * 
 */
public class SpanAligner2 {
    private static final int ROOT = -1;
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
			} else if ((rootSpanId == null || rootSpanId != ROOT) && span.getStartTime() < rootSpanStartTime) {
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

	private void populate(SpanBo parentSpan, int spanDepth, int sequence, int pSequence, List<SpanAlign> container) {
		int depth = spanDepth + 1;

		SpanAlign element = new SpanAlign(depth, parentSpan, ++sequence, pSequence);
		container.add(element);

		List<SpanEventBo> spanEventBoList = parentSpan.getSpanEventBoList();
        if (spanEventBoList == null) {
            return;
        }
        
        element.setHasChild(true);
        
        int lastChildSequence = sequence;
		for (SpanEventBo spanEventBo : spanEventBoList) {
			if (spanEventBo.getDepth() != -1) {
				depth = spanDepth + spanEventBo.getDepth() + 1;
			}
			
			lastChildSequence++;
			
			SpanAlign sa = new SpanAlign(depth, lastChildSequence, sequence, parentSpan, spanEventBo);
			container.add(sa);

			// TODO spanEvent이 drop되면 container에 채워지지 못하는 Span이 생길 수 있다.
			int nextSpanId = spanEventBo.getNextSpanId();
			if (nextSpanId != ROOT && spanMap.containsKey(nextSpanId)) {
				populate(spanMap.get(nextSpanId), depth, lastChildSequence, lastChildSequence, container);
			}
		}
	}
}

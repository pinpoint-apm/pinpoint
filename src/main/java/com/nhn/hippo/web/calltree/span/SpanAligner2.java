package com.nhn.hippo.web.calltree.span;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.profiler.common.bo.SpanBo;
import com.profiler.common.bo.SubSpanBo;

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

		populate(root, 0, list);

		System.out.println(list);

		return list;
	}

	private void populate(SpanBo parentSpan, int spanDepth, List<SpanAlign> container) {
		int depth = spanDepth + 1;

		SpanAlign element = new SpanAlign(depth, parentSpan);
		container.add(element);

		List<SubSpanBo> subSpanList = parentSpan.getSubSpanList();
		for (SubSpanBo subSpanBo : subSpanList) {
			if (subSpanBo.getDepth() != -1) {
				depth = spanDepth + subSpanBo.getDepth() + 1;
			}

			SpanAlign sa = new SpanAlign(depth, parentSpan, subSpanBo);
			container.add(sa);

			int nextSpanId = subSpanBo.getNextSpanId();
			if (nextSpanId != ROOT && spanMap.containsKey(nextSpanId)) {
				populate(spanMap.get(nextSpanId), depth, container);
			}
		}
	}
}

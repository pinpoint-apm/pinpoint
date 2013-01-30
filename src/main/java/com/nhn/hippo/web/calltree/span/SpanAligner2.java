package com.nhn.hippo.web.calltree.span;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.profiler.common.bo.SpanBo;
import com.profiler.common.bo.SubSpanBo;

/**
 * 
 * @author netspider
 * 
 */
public class SpanAligner2 {

	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	private final Map<Long, SpanBo> spanMap;
	private Long rootSpanId;

	public SpanAligner2(List<SpanBo> spans) {
		spanMap = new HashMap<Long, SpanBo>(spans.size());

		long rootSpanStartTime = Long.MAX_VALUE;

		for (SpanBo span : spans) {
			if (spanMap.containsKey(Long.valueOf(span.getSpanId()))) {
				throw new IllegalStateException("duplicated spanId. id:" + span.getSpanId());
			}

			if (span.getStartTime() < rootSpanStartTime) {
				rootSpanId = (span.getParentSpanId() == -1) ? -1L : span.getSpanId();
				rootSpanStartTime = span.getStartTime();
			}

			this.spanMap.put((span.getParentSpanId() == -1) ? -1L : span.getSpanId(), span);
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

			Long nextSpanId = Long.valueOf(subSpanBo.getNextSpanId());
			if (nextSpanId != -1 && spanMap.containsKey(nextSpanId)) {
				populate(spanMap.get(Long.valueOf(nextSpanId)), depth, container);
			}
		}
	}
}

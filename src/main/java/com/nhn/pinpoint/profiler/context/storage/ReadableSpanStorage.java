package com.nhn.pinpoint.profiler.context.storage;

import java.util.ArrayList;
import java.util.List;

import com.nhn.pinpoint.bootstrap.context.ReadableStorage;
import com.nhn.pinpoint.common.bo.SpanEventBo;
import com.nhn.pinpoint.profiler.context.Span;
import com.nhn.pinpoint.profiler.context.SpanEvent;
import com.nhn.pinpoint.profiler.context.Storage;

/**
 * @author Hyun Jeong
 */
public final class ReadableSpanStorage implements Storage, ReadableStorage {

	private List<SpanEventBo> spanEventList = new ArrayList<SpanEventBo>(10);
	
	@Override
	public void store(SpanEvent spanEvent) {
		if (spanEvent == null) {
			throw new NullPointerException("spanEvent must not be null");
		}
		final List<SpanEventBo> spanEventList = this.spanEventList;
		if (spanEventList != null) {
			final SpanEventBo spanEventBo = new SpanEventBo(spanEvent.getSpan(), spanEvent);
			spanEventList.add(spanEventBo);
		} else {
			throw new IllegalStateException("spanEventList is null");
		}
	}

	@Override
	public void store(Span span) {
		if (span == null) {
			throw new NullPointerException("span must not be null");
		}
		this.spanEventList = null;
	}

	@Override
	public List<SpanEventBo> getSpanEventList() {
		if (this.spanEventList == null) {
			throw new IllegalStateException("Trace not initialized or already completed.");
		}
		return this.spanEventList;
	}

}

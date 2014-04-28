package com.nhn.pinpoint.profiler.junit4;

import java.util.List;

import org.junit.runner.RunWith;

import com.nhn.pinpoint.profiler.context.ReadableStorage;
import com.nhn.pinpoint.profiler.context.SpanEvent;
import com.nhn.pinpoint.profiler.util.TestClassLoader;

/**
 * @author Hyun Jeong
 */
@RunWith(value=PinpointJUnit4ClassRunner.class)
@PinpointTestClassLoader(TestClassLoader.class)
public abstract class BasePinpointTest {
	private ThreadLocal<ReadableStorage> traceHolder = new ThreadLocal<ReadableStorage>();
	
	@SuppressWarnings("unchecked")
	protected final List<SpanEvent> getCurrentSpanEvents() {
		return (List<SpanEvent>)this.traceHolder.get().getSpanEventList();
	}
	
	final void setCurrentStorage(ReadableStorage spanStorage) {
		traceHolder.set(spanStorage);
	}
}

package com.nhn.pinpoint.profiler.context;

import java.util.List;

import com.nhn.pinpoint.thrift.dto.TSpanEvent;

/**
 * @author Hyun Jeong
 */
public interface ReadableStorage extends Storage {
	public  List<? extends TSpanEvent> getSpanEventList();
}

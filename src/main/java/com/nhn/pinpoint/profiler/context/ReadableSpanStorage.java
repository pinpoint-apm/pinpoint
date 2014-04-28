package com.nhn.pinpoint.profiler.context;

import java.util.List;

import com.nhn.pinpoint.profiler.sender.DataSender;
import com.nhn.pinpoint.thrift.dto.TSpanEvent;

/**
 * @author Hyun Jeong
 */
public class ReadableSpanStorage extends SpanStorage implements ReadableStorage {

	public ReadableSpanStorage(DataSender dataSender) {
		super(dataSender);
	}

	@Override
	public List<? extends TSpanEvent> getSpanEventList() {
		return super.spanEventList;
	}

}

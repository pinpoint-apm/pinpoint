package com.nhn.pinpoint.profiler.context;

import com.nhn.pinpoint.profiler.sender.DataSender;

/**
 * @author Hyun Jeong
 */
public class ReadableSpanStorageFactory extends SpanStorageFactory {

	public ReadableSpanStorageFactory(DataSender dataSender) {
		super(dataSender);
	}

	@Override
	public Storage createStorage() {
		return new ReadableSpanStorage(super.dataSender);
	}

}

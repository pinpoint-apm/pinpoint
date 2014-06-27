package com.nhn.pinpoint.profiler.context.storage;

import com.nhn.pinpoint.profiler.context.Storage;
import com.nhn.pinpoint.profiler.context.StorageFactory;

/**
 * @author Hyun Jeong
 */
public class ReadableSpanStorageFactory implements StorageFactory {

	@Override
	public Storage createStorage() {
		return new ReadableSpanStorage();
	}

}

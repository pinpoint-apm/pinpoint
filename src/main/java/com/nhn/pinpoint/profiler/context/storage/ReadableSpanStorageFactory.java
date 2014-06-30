package com.nhn.pinpoint.profiler.context.storage;

/**
 * @author Hyun Jeong
 */
public class ReadableSpanStorageFactory implements StorageFactory {

	@Override
	public Storage createStorage() {
		return new ReadableSpanStorage();
	}

}

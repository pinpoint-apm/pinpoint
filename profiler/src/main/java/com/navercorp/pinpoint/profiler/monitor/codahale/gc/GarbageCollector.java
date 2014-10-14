package com.nhn.pinpoint.profiler.monitor.codahale.gc;

import com.nhn.pinpoint.thrift.dto.TJvmGc;

/**
 * @author harebox
 * @author emeroad
 */
public interface GarbageCollector {

	int getTypeCode();

	TJvmGc collect();

}

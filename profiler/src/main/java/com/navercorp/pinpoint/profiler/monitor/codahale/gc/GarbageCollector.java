package com.navercorp.pinpoint.profiler.monitor.codahale.gc;

import com.navercorp.pinpoint.thrift.dto.TJvmGc;

/**
 * @author harebox
 * @author emeroad
 */
public interface GarbageCollector {

	int getTypeCode();

	TJvmGc collect();

}

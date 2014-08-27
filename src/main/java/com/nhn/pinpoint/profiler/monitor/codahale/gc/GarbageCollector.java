package com.nhn.pinpoint.profiler.monitor.codahale.gc;

import com.nhn.pinpoint.profiler.monitor.codahale.MetricMonitorRegistry;
import com.nhn.pinpoint.thrift.dto.TJvmGc;
import com.nhn.pinpoint.thrift.dto.TJvmGcType;

/**
 * @author harebox
 * @author emeroad
 */
public interface GarbageCollector {

	int getTypeCode();

	TJvmGc collect();

}

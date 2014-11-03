package com.nhn.pinpoint.profiler.receiver;

import org.apache.thrift.TBase;

/**
 * @author koo.taejin
 */
public interface ProfilerRequestCommandService extends ProfilerCommandService {

	TBase<?, ?> requestCommandService(TBase tBase);
	
}

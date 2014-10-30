package com.nhn.pinpoint.profiler.receiver;

import org.apache.thrift.TBase;

/**
 * @author koo.taejin
 */
public interface ProfilerSimpleCommandService extends ProfilerCommandService {

	void simpleCommandService(TBase<?, ?> tbase);
	
}

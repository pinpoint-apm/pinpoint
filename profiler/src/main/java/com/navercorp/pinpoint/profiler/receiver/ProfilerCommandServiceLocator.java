package com.nhn.pinpoint.profiler.receiver;

import org.apache.thrift.TBase;

/**
 * @author koo.taejin
 */
public interface ProfilerCommandServiceLocator {

	ProfilerCommandService getService(TBase tBase);

	ProfilerSimpleCommandService getSimpleService(TBase tBase);

	ProfilerRequestCommandService getRequestService(TBase tBase);

}

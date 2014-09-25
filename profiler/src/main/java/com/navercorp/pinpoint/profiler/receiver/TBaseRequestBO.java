package com.nhn.pinpoint.profiler.receiver;

import org.apache.thrift.TBase;

/**
 * @author koo.taejin
 */
public interface TBaseRequestBO extends TBaseBO {

	TBase<?, ?> handleRequest(TBase tBase);
	
}

package com.nhn.pinpoint.profiler.receiver;

import org.apache.thrift.TBase;

/**
 * @author koo.taejin
 */
public interface TBaseSimpleBO extends TBaseBO {

	void handleSimple(TBase<?, ?> tbase);
	
}

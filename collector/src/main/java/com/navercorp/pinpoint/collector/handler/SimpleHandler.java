package com.nhn.pinpoint.collector.handler;

import org.apache.thrift.TBase;

/**
 * @author emeroad
 * @author koo.taejin
 */
public interface SimpleHandler {
	
    void handleSimple(TBase<?, ?> tbase);
    
}

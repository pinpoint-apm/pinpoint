package com.nhn.pinpoint.collector.handler;

import org.apache.thrift.TBase;

/**
 * @author emeroad
 * @author koo.taejin
 */
public interface Handler {
	
    void handle(TBase<?, ?> tbase, byte[] packet, int offset, int length);
    
}

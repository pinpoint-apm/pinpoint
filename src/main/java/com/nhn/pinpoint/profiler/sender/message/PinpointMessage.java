package com.nhn.pinpoint.profiler.sender.message;

import org.apache.thrift.TBase;

/**
 * @author koo.taejin
 */
public interface PinpointMessage {

	TBase getTBase();
	
	byte[] serialize();
	
}

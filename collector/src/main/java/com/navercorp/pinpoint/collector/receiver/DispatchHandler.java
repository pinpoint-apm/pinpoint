package com.nhn.pinpoint.collector.receiver;

import org.apache.thrift.TBase;

/**
 * @author emeroad
 * @author koo.taejin
 */
public interface DispatchHandler {

	// Send와 Request를 분리한다. 형태 자체가 썩 맘에 들지는 않지만 이후에 변경하자 
	
	void dispatchSendMessage(TBase<?, ?> tBase, byte[] packet, int offset, int length);

	TBase dispatchRequestMessage(TBase<?, ?> tBase, byte[] packet, int offset, int length);
	
}

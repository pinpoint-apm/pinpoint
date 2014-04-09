package com.nhn.pinpoint.collector.handler;

import org.apache.thrift.TBase;
import org.springframework.stereotype.Service;

/**
 * @author emeroad
 * @author koo.taejin
 */
@Service
public interface RequestResponseHandler {
	
    TBase<?, ?> handleRequest(TBase<?, ?> tbase);
    
}

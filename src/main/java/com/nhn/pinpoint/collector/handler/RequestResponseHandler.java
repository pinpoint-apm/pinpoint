package com.nhn.pinpoint.collector.handler;

import org.apache.thrift.TBase;
import org.springframework.stereotype.Service;

/**
 * @author emeroad
 */
@Service
public interface RequestResponseHandler {
    TBase<?, ?> handler(TBase<?, ?> tbase);
}

package com.nhn.pinpoint.collector.handler;

import org.apache.thrift.TBase;
import org.springframework.stereotype.Service;

/**
 *
 */
@Service
public interface RequestResponseHandler {
    TBase<?, ?> handler(TBase<?, ?> tbase);
}

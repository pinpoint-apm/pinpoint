package com.nhn.pinpoint.collector.receiver;

import com.nhn.pinpoint.collector.handler.SimpleHandler;
import com.nhn.pinpoint.thrift.dto.*;
import org.apache.thrift.TBase;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

public class UdpSpanDispatchHandler extends AbstractDispatchHandler {


    @Autowired()
    @Qualifier("spanHandler")
    private SimpleHandler spanDataHandler;

    @Autowired()
    @Qualifier("spanEventHandler")
    private SimpleHandler spanEventHandler;

    @Autowired()
    @Qualifier("spanChunkHandler")
    private SimpleHandler spanChunkHandler;

    public UdpSpanDispatchHandler() {
        this.logger = LoggerFactory.getLogger(this.getClass());
    }



    @Override
    SimpleHandler getSimpleHandler(TBase<?, ?> tBase) {
        if (tBase instanceof Span) {
            return spanDataHandler;
        }
        if (tBase instanceof SpanEvent) {
            return spanEventHandler;
        }
        if (tBase instanceof SpanChunk) {
            return spanChunkHandler;
        }

        return null;
    }
}

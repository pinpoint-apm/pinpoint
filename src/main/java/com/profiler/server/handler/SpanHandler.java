package com.profiler.server.handler;

import java.net.DatagramPacket;

import com.profiler.common.dto.Header;
import com.profiler.common.util.PacketUtils;
import com.profiler.server.dao.TraceIndex;
import com.profiler.server.dao.Traces;
import org.apache.thrift.TBase;

import com.profiler.common.dto.thrift.Span;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

public class SpanHandler implements Handler {

    private final Logger logger = LoggerFactory.getLogger(SpanHandler.class.getName());

    @Autowired
    private TraceIndex traceIndex;

    @Autowired
    private Traces trace;

    public void handler(TBase<?, ?> tbase, DatagramPacket datagramPacket) {
        assert (tbase instanceof Span);

        try {
            Span span = (Span) tbase;
            byte[] spanBytes = PacketUtils.sliceData(datagramPacket, Header.HEADER_SIZE);
            trace.insert(span, spanBytes);
            traceIndex.insert(span);

            if (logger.isInfoEnabled()) {
                logger.info("Received SPAN=" + span);
            }
        } catch (Exception e) {
            logger.warn("Span handle error " + e.getMessage(), e);
        }
    }
}

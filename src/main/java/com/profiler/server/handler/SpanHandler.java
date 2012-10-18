package com.profiler.server.handler;

import java.net.DatagramPacket;

import com.profiler.server.dao.TraceIndex;
import com.profiler.server.dao.TraceDao;
import org.apache.thrift.TBase;

import com.profiler.common.dto.thrift.Span;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

public class SpanHandler implements Handler {

    private final Logger logger = LoggerFactory.getLogger(SpanHandler.class.getName());

    @Autowired
    private TraceIndex traceIndexDao;

    @Autowired
    private TraceDao traceDao;

    public void handler(TBase<?, ?> tbase, DatagramPacket datagramPacket) {
        assert (tbase instanceof Span);

        try {
            Span span = (Span) tbase;
            traceDao.insert(span);
            traceIndexDao.insert(span);

            if (logger.isInfoEnabled()) {
                logger.info("Received SPAN={}", span);
            }
        } catch (Exception e) {
            logger.warn("Span handle error " + e.getMessage(), e);
        }
    }
}

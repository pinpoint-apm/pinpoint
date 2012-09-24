package com.profiler.server.data.handler;

import java.net.DatagramPacket;

import com.profiler.server.dao.TraceIndex;
import com.profiler.server.dao.Traces;
import org.apache.log4j.Logger;
import org.apache.thrift.TBase;

import com.profiler.common.dto.thrift.Span;
import org.springframework.beans.factory.annotation.Autowired;

public class SpanHandler implements Handler {

	private final Logger logger = Logger.getLogger(SpanHandler.class.getName());

    @Autowired
	private TraceIndex traceIndex;

    @Autowired
	private Traces trace;

	public void handler(TBase<?, ?> tbase, DatagramPacket datagramPacket) {
		assert (tbase instanceof Span);

		try {
			Span span = (Span) tbase;

            trace.insert(span);
            traceIndex.insert(span);

			if (logger.isInfoEnabled()) {
				logger.info("Received SPAN=" + span);
			}
		} catch (Exception e) {
			logger.warn("Span handle error " + e.getMessage(), e);
		}
	}
}

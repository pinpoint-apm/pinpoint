package com.profiler.server.data.reader;

import java.net.DatagramPacket;

import org.apache.log4j.Logger;
import org.apache.thrift.TBase;

import com.profiler.common.dto.thrift.Span;
import com.profiler.server.datasource.TraceIndex;
import com.profiler.server.datasource.Traces;

public class SpanReader implements Reader {
	private final Logger logger = Logger.getLogger(SpanReader.class.getName());

	private final TraceIndex tindex = new TraceIndex();
	private final Traces trace = new Traces();

	public void handler(TBase<?, ?> tbase, DatagramPacket datagramPacket) {
		assert (tbase instanceof Span);

		try {
			Span span = (Span) tbase;

			tindex.insert(span);
			trace.insert(span);

			if (logger.isInfoEnabled()) {
				logger.info("Received SPAN=" + span);
			}
		} catch (Exception e) {
			logger.warn("ReadJVMData handle error " + e.getMessage(), e);
		}
	}
}

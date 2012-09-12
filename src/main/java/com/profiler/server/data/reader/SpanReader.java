package com.profiler.server.data.reader;

import java.net.DatagramPacket;

import org.apache.log4j.Logger;
import org.apache.thrift.TBase;

import com.profiler.common.dto.thrift.Span;

public class SpanReader implements Reader {
	private final Logger logger = Logger.getLogger(SpanReader.class.getName());

	public void handler(TBase<?, ?> tbase, DatagramPacket datagramPacket) {
		assert (tbase instanceof Span);

		try {
			Span span = (Span) tbase;

			if (logger.isInfoEnabled()) {
				logger.info("Received SPAN=" + span);
			}
		} catch (Exception e) {
			logger.warn("ReadJVMData handle error " + e.getMessage(), e);
		}
	}
}

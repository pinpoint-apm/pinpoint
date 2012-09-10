package com.profiler.data.reader;

import java.net.DatagramPacket;

import org.apache.log4j.Logger;
import org.apache.thrift.TBase;

import com.profiler.context.gen.Span;

public class SpanReader implements Reader {
	private static final Logger logger = Logger.getLogger(SpanReader.class.getName());

	public void handler(TBase<?, ?> tbase, DatagramPacket datagramPacket) {
		if (logger.isDebugEnabled()) {
			logger.debug("handle " + tbase);
		}
		try {
			Span span = (Span) tbase;
			System.out.println("span=" + span);
		} catch (Exception e) {
			logger.warn("ReadJVMData handle error " + e.getMessage(), e);
		}
	}
}

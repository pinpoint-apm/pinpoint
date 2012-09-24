package com.profiler.server.data.handler;

import java.net.DatagramPacket;

import org.apache.log4j.Logger;
import org.apache.thrift.TBase;

import com.profiler.common.dto.thrift.JVMInfoThriftDTO;

public class JVMDataHandler implements Handler {
	private final Logger logger = Logger.getLogger(JVMDataHandler.class.getName());

	public void handler(TBase<?, ?> tbase, DatagramPacket datagramPacket) {
		assert (tbase instanceof JVMInfoThriftDTO);

		try {
			JVMInfoThriftDTO dto = (JVMInfoThriftDTO) tbase;

			if (logger.isInfoEnabled()) {
				logger.info("Received JVM=" + dto);
			}
		} catch (Exception e) {
			logger.warn("JVMData handle error " + e.getMessage(), e);
		}
	}
}

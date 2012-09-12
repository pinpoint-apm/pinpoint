package com.profiler.server.data.reader;

import java.net.DatagramPacket;

import org.apache.log4j.Logger;
import org.apache.thrift.TBase;

import com.profiler.common.dto.thrift.JVMInfoThriftDTO;

public class JVMDataReader implements Reader {
	private final Logger logger = Logger.getLogger(JVMDataReader.class.getName());

	public void handler(TBase<?, ?> tbase, DatagramPacket datagramPacket) {
		assert (tbase instanceof JVMInfoThriftDTO);

		try {
			JVMInfoThriftDTO dto = (JVMInfoThriftDTO) tbase;

			if (logger.isInfoEnabled()) {
				logger.info("Received JVM=" + dto);
			}
		} catch (Exception e) {
			logger.warn("ReadJVMData handle error " + e.getMessage(), e);
		}
	}
}

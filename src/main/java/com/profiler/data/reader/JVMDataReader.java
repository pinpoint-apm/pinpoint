package com.profiler.data.reader;

import java.net.DatagramPacket;

import org.apache.log4j.Logger;
import org.apache.thrift.TBase;

import com.profiler.common.dto.thrift.JVMInfoThriftDTO;

public class JVMDataReader implements Reader {
	private static final Logger logger = Logger.getLogger(JVMDataReader.class.getName());

	public JVMDataReader() {
	}

	public void handler(TBase<?, ?> tbase, DatagramPacket datagramPacket) {
		if (logger.isDebugEnabled()) {
			logger.debug("handle " + tbase);
		}
		try {
			JVMInfoThriftDTO dto = (JVMInfoThriftDTO) tbase;

			System.out.println("Got JVM DTO. " + dto);
		} catch (Exception e) {
			logger.warn("ReadJVMData handle error " + e.getMessage(), e);
		}
	}
}

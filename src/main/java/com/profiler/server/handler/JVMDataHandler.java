package com.profiler.server.handler;

import java.net.DatagramPacket;

import org.apache.thrift.TBase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.profiler.common.dto.Header;
import com.profiler.common.dto.thrift.JVMInfoThriftDTO;
import com.profiler.common.util.PacketUtils;
import com.profiler.server.dao.JvmInfoDao;

public class JVMDataHandler implements Handler {

	private final Logger logger = LoggerFactory.getLogger(JVMDataHandler.class.getName());

	@Autowired
	private JvmInfoDao jvmInfoDao;

	public void handler(TBase<?, ?> tbase, DatagramPacket datagramPacket) {
		assert (tbase instanceof JVMInfoThriftDTO);

		try {
			JVMInfoThriftDTO dto = (JVMInfoThriftDTO) tbase;
			
			if (logger.isInfoEnabled()) {
				logger.info("Received JVM=" + dto);
			}
			
			byte[] bytes = PacketUtils.sliceData(datagramPacket, Header.HEADER_SIZE);

			jvmInfoDao.insert(dto, bytes);
		} catch (Exception e) {
			logger.warn("JVMData handle error. Caused:" + e.getMessage(), e);
		}
	}
}

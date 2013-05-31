package com.profiler.server.handler;

import com.nhn.pinpoint.common.dto2.Header;
import com.nhn.pinpoint.common.dto2.thrift.JVMInfoThriftDTO;
import com.nhn.pinpoint.common.io.PacketUtils;
import com.profiler.server.dao.JvmInfoDao;
import org.apache.thrift.TBase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.net.DatagramPacket;

public class JVMDataHandler implements Handler {

    private final Logger logger = LoggerFactory.getLogger(JVMDataHandler.class.getName());

    @Autowired
    private JvmInfoDao jvmInfoDao;

    public void handler(TBase<?, ?> tbase, DatagramPacket datagramPacket) {
        if (!(tbase instanceof JVMInfoThriftDTO)) {
            throw new IllegalArgumentException("unexpected tbase:" + tbase + " expected:" + this.getClass().getName());
        }

        try {
            JVMInfoThriftDTO dto = (JVMInfoThriftDTO) tbase;

            if (logger.isInfoEnabled()) {
                logger.info("Received JVM={}", dto);
            }

            byte[] bytes = PacketUtils.sliceData(datagramPacket, Header.HEADER_SIZE);

            jvmInfoDao.insert(dto, bytes);
        } catch (Exception e) {
            logger.warn("JVMData handle error. Caused:" + e.getMessage(), e);
        }
    }
}

package com.nhn.pinpoint.collector.handler;

import com.nhn.pinpoint.thrift.io.Header;
import com.nhn.pinpoint.thrift.dto.TJVMInfoThriftDTO;
import com.nhn.pinpoint.thrift.io.PacketUtils;
import com.nhn.pinpoint.collector.dao.JvmInfoDao;
import org.apache.thrift.TBase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class JVMDataHandler implements Handler {

    private final Logger logger = LoggerFactory.getLogger(JVMDataHandler.class.getName());

    @Autowired
    private JvmInfoDao jvmInfoDao;

    public void handler(TBase<?, ?> tbase, byte[] packet, int offset, int length) {
        if (!(tbase instanceof TJVMInfoThriftDTO)) {
            throw new IllegalArgumentException("unexpected tbase:" + tbase + " expected:" + this.getClass().getName());
        }

        try {
            TJVMInfoThriftDTO dto = (TJVMInfoThriftDTO) tbase;

            if (logger.isDebugEnabled()) {
                logger.debug("Received JVM={}", dto);
            }

            byte[] bytes = PacketUtils.sliceData(packet, Header.HEADER_SIZE, length);

            jvmInfoDao.insert(dto, bytes);
        } catch (Exception e) {
            logger.warn("JVMData handle error. Caused:{}", e.getMessage(), e);
        }
    }
}

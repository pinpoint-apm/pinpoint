package com.profiler.server.handler;

import com.profiler.common.dto.thrift.Span;
import com.profiler.common.dto.thrift.SqlMetaData;
import com.profiler.server.dao.SqlMetaDataDao;
import org.apache.thrift.TBase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.net.DatagramPacket;

/**
 *
 */
public class SqlMetaDataHandler implements Handler {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private SqlMetaDataDao sqlMetaDataDao;

    @Override
    public void handler(TBase<?, ?> tbase, DatagramPacket datagramPacket) {
        if (!(tbase instanceof SqlMetaData)) {
            logger.warn("invalid tbase:" + tbase);
            return;
        }
        SqlMetaData sqlMetaData = (SqlMetaData) tbase;
        if (logger.isInfoEnabled()) {
            logger.info("Received SqlMetaData{}", sqlMetaData);
        }
        sqlMetaDataDao.insert(sqlMetaData);

    }
}

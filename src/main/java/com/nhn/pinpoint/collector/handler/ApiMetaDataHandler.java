package com.nhn.pinpoint.collector.handler;

import com.nhn.pinpoint.common.dto2.thrift.ApiMetaData;
import com.nhn.pinpoint.collector.dao.ApiMetaDataDao;
import org.apache.thrift.TBase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.net.DatagramPacket;

/**
 *
 */
public class ApiMetaDataHandler implements SimpleHandler {

	private final Logger logger = LoggerFactory.getLogger(getClass());

	@Autowired
	private ApiMetaDataDao sqlMetaDataDao;

	@Override
	public void handler(TBase<?, ?> tbase) {
		if (!(tbase instanceof ApiMetaData)) {
			logger.warn("invalid tbase:{}", tbase);
			return;
		}
		
		ApiMetaData apiMetaData = (ApiMetaData) tbase;
        // api 데이터는 중요한거니 그냥 info로 찍음.
		if (logger.isInfoEnabled()) {
			logger.info("Received ApiMetaData={}", apiMetaData);
		}
		
		sqlMetaDataDao.insert(apiMetaData);
	}
}

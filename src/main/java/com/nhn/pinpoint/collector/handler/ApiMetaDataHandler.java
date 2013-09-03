package com.nhn.pinpoint.collector.handler;

import com.nhn.pinpoint.common.dto2.thrift.ApiMetaData;
import com.nhn.pinpoint.collector.dao.ApiMetaDataDao;
import com.nhn.pinpoint.common.dto2.thrift.Result;
import org.apache.thrift.TBase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.net.DatagramPacket;

/**
 *
 */
@Service
public class ApiMetaDataHandler implements RequestResponseHandler {

	private final Logger logger = LoggerFactory.getLogger(getClass());

	@Autowired
	private ApiMetaDataDao sqlMetaDataDao;

	@Override
	public TBase<?, ?> handler(TBase<?, ?> tbase) {
		if (!(tbase instanceof ApiMetaData)) {
			logger.error("invalid tbase:{}", tbase);
			return null;
		}
		
		ApiMetaData apiMetaData = (ApiMetaData) tbase;
        // api 데이터는 중요한거니 그냥 info로 찍음.
		if (logger.isInfoEnabled()) {
			logger.info("Received ApiMetaData={}", apiMetaData);
		}

        try {
            sqlMetaDataDao.insert(apiMetaData);
        } catch (Exception e) {
            logger.warn("{} handler error. Caused:{}", this.getClass(), e.getMessage(), e);
            Result result = new Result(false);
            result.setMessage(e.getMessage());
            return result;
        }
        return new Result(true);
	}
}

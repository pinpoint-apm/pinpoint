package com.profiler.data.read;

import java.net.DatagramPacket;

import org.apache.log4j.Logger;
import org.apache.thrift.TBase;

import com.profiler.dto.RequestThriftDTO;

public class ReadRequestTransactionData implements ReadHandler {

    private static final Logger logger = Logger.getLogger(ReadRequestTransactionData.class.getName());


	public ReadRequestTransactionData() {
	}

	public void handler(TBase<?, ?> tbase, DatagramPacket datagramPacket) {
        if (logger.isDebugEnabled()) {
            logger.debug("handle " + tbase);
        }

		try {
//            RequestThriftDTO dto = (RequestThriftDTO) tbase;
//			RequestTransactionDataManager manager = new RequestTransactionDataManager();
//			manager.addRequest(dto);
		} catch (Exception e) {
			logger.warn("ReadRequestTransactionData handle error " + e.getMessage(), e);
		}
	}
}

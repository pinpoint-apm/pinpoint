package com.profiler.data.read;

import org.apache.log4j.Logger;
import org.apache.thrift.TBase;
import org.apache.thrift.TDeserializer;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.transport.TTransportException;

import com.profiler.data.manager.RequestTransactionDataManager;
import com.profiler.dto.RequestThriftDTO;

import java.net.DatagramPacket;

public class ReadRequestTransactionData implements ReadHandler {

    private static final Logger logger = Logger.getLogger(ReadRequestTransactionData.class.getName());


	public ReadRequestTransactionData() {
	}

	public void handler(TBase<?, ?> tbase, DatagramPacket datagramPacket) {
        if (logger.isDebugEnabled()) {
            logger.debug("handle " + tbase);
        }

		try {
            RequestThriftDTO dto = (RequestThriftDTO) tbase;
			RequestTransactionDataManager manager = new RequestTransactionDataManager();
			manager.addRequest(dto);
		} catch (Exception e) {
			logger.warn("ReadRequestTransactionData handle error " + e.getMessage(), e);
		}
	}
}

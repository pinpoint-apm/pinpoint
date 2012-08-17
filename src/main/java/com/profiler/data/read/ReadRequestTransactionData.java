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
	private final Logger logger = Logger.getLogger(this.getClass().getName());

	long receiveTime=0;

	public ReadRequestTransactionData() {
	}

	public ReadRequestTransactionData(long receiveTime) {
		this.receiveTime=receiveTime;
	}

	public void handler(TBase<?, ?> tbase, DatagramPacket datagramPacket) {
        if (logger.isDebugEnabled()) {
            logger.debug("handle " + tbase);
        }
        RequestThriftDTO dto = (RequestThriftDTO) tbase;
		try {
			logger.debug(dto);
			RequestTransactionDataManager manager=new RequestTransactionDataManager();
			manager.addRequest(dto);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}

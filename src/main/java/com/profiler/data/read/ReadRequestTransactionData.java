package com.profiler.data.read;

import org.apache.log4j.Logger;
import org.apache.thrift.TDeserializer;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.transport.TTransportException;

import com.profiler.data.manager.RequestTransactionDataManager;
import com.profiler.dto.RequestThriftDTO;

public class ReadRequestTransactionData extends Thread{
	private static final Logger logger = Logger.getLogger("RequestInfo");
	long receiveTime=0;
	byte[] data=null;
	public ReadRequestTransactionData(byte[] data) {
		this.data=data;
		
	}
	public ReadRequestTransactionData(long receiveTime,byte[] data) {
		this.receiveTime=receiveTime;
		this.data=data;
	}
	public void run() {
		TDeserializer deserializer = new TDeserializer(new TBinaryProtocol.Factory());
		RequestThriftDTO dto=new RequestThriftDTO();
		try {
			deserializer.deserialize(dto,data);
			logger.debug(dto);
			RequestTransactionDataManager manager=new RequestTransactionDataManager();
			manager.addRequest(dto);
		} catch (TTransportException tte) {
			logger.error(tte.getMessage());
			System.out.println("Message size="+data.length);
			tte.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}

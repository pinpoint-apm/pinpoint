package com.profiler.data.read;

import org.apache.log4j.Logger;
import org.apache.thrift.TDeserializer;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.transport.TTransportException;

import com.profiler.data.manager.RequestTransactionDataManager;
import com.profiler.dto.RequestDataListThriftDTO;

public class ReadRequestData extends Thread{
	private static final Logger logger = Logger.getLogger("RequestInfo");
	long receiveTime=0;
	byte[] data=null;
	public ReadRequestData(byte[] data) {
		this.data=data;
	}
	public ReadRequestData(long receiveTime,byte[] data) {
		this.receiveTime=receiveTime;
		this.data=data;
	}
	public void run() {
		TDeserializer deserializer = new TDeserializer(new TBinaryProtocol.Factory());
		RequestDataListThriftDTO dto=new RequestDataListThriftDTO();
		try {
//			System.out.println(receiveTime+" "+data.length);//+" "+new String(data));
			deserializer.deserialize(dto,data);
//			logger.debug(dto);
			RequestTransactionDataManager manager=new RequestTransactionDataManager();
			
			//For Debug start
			int hostHashCode=dto.getHostHashCode();
			CharSequence agentName=manager.getAgentName(hostHashCode);
			int requestHashCode=dto.getRequestHashCode();
			String url=manager.getRequestURL(requestHashCode);
			StringBuilder message=RequestDataPrinter.printRequestData(dto, agentName, url,"\n");
			logger.debug(message);
			//For Debug end
			
			manager.addRequestDataList(dto, data);
			
		} catch (TTransportException tte) {
			logger.error(tte.getMessage());
			System.out.println("Message size="+data.length);
			tte.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
}

package com.profiler.data.read;

import org.apache.log4j.Logger;
import org.apache.thrift.TBase;

import com.profiler.data.manager.RequestTransactionDataManager;
import com.profiler.dto.RequestDataListThriftDTO;

import java.lang.reflect.Array;
import java.net.DatagramPacket;
import java.util.Arrays;

public class ReadRequestData implements ReadHandler {
	private static final Logger logger = Logger.getLogger("RequestInfo");

    long receiveTime=0;

    public ReadRequestData() {
	}

	public ReadRequestData(long receiveTime) {
		this.receiveTime=receiveTime;
	}

	public void handler(TBase<?, ?> tbase, DatagramPacket datagramPacket) {
        if (logger.isDebugEnabled()) {
            logger.debug("handle " + tbase);
        }
        RequestDataListThriftDTO dto = (RequestDataListThriftDTO) tbase;
		try {
			RequestTransactionDataManager manager=new RequestTransactionDataManager();
			
			//For Debug start
			int hostHashCode=dto.getHostHashCode();
			CharSequence agentName=manager.getAgentName(hostHashCode);
			int requestHashCode=dto.getRequestHashCode();
			String url=manager.getRequestURL(requestHashCode);
			StringBuilder message=RequestDataPrinter.printRequestData(dto, agentName, url,"\n");
			logger.debug(message);
			//For Debug end

			// TODO packet을 slice해서 넣어야 될거 같음
            manager.addRequestDataList(dto, Arrays.copyOf(datagramPacket.getData(), datagramPacket.getLength()));
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}


}

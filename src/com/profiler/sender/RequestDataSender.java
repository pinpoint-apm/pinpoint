package com.profiler.sender;

import java.net.InetSocketAddress;

import org.apache.thrift.TSerializer;
import org.apache.thrift.protocol.TBinaryProtocol;

import com.profiler.config.TomcatProfilerConfig;
import com.profiler.dto.RequestDataListThriftDTO;

public class RequestDataSender extends AbstractDataSender{
	RequestDataListThriftDTO dto;
	public RequestDataSender(RequestDataListThriftDTO dto) {
		this.dto=dto;
//		log(dto.toString());
	}
	@Override
	protected byte[] getSendData() throws Exception {
		TSerializer serializer = new TSerializer(new TBinaryProtocol.Factory());
//		byte[] serializedBytes=serializer.serialize(dto);
//		System.out.println("-----Request Data size="+serializedBytes.length);
//		return serializedBytes;
		return serializer.serialize(dto);
	}

	@Override
	protected InetSocketAddress getAddress() throws Exception {
		return new InetSocketAddress(TomcatProfilerConfig.SERVER_IP, TomcatProfilerConfig.REQUEST_DATA_LISTEN_PORT);
	}


}

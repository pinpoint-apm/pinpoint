package com.profiler.sender;

import java.net.InetSocketAddress;

import org.apache.thrift.TSerializer;
import org.apache.thrift.protocol.TBinaryProtocol;

import com.profiler.config.TomcatProfilerConfig;
import com.profiler.dto.JVMInfoThriftDTO;

public class JVMDataSender extends AbstractDataSender{
	JVMInfoThriftDTO dto;
	public JVMDataSender(JVMInfoThriftDTO dto) {
		this.dto=dto;
		log(dto.toString());
	}
	protected byte[] getSendData() throws Exception {
		TSerializer serializer = new TSerializer(new TBinaryProtocol.Factory());
		return serializer.serialize(dto);
	}
	protected InetSocketAddress getAddress() throws Exception {
		return new InetSocketAddress(TomcatProfilerConfig.SERVER_IP, TomcatProfilerConfig.JVM_DATA_LISTEN_PORT);
	}
}

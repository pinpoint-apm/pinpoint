package com.profiler.data.store.hbase.thrift;

import static com.profiler.config.TomcatProfilerReceiverConfig.HBASE_THRIFT_IP;
import static com.profiler.config.TomcatProfilerReceiverConfig.HBASE_THRIFT_PORT;

import org.apache.hadoop.hbase.thrift2.generated.THBaseService;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.transport.TNonblockingTransport;
import org.apache.thrift.transport.TSocket;
import org.apache.thrift.transport.TTransport;
public class Thrift2ClientManager {
	public Thrift2ClientManager() {}
	TTransport transport;
	TNonblockingTransport nonblockTransport;
	public THBaseService.Client getClient() throws Exception{
		THBaseService.Client client;
		transport=new TSocket(HBASE_THRIFT_IP,HBASE_THRIFT_PORT);
		TProtocol protocol=new TBinaryProtocol(transport,true,true);
		client = new THBaseService.Client(protocol);
		transport.open();
		return client;
	}
//	public THBaseService.AsyncClient getAsyncClient() throws Exception{
//		THBaseService.AsyncClient client;
//		nonblockTransport=new TNonblockingSocket(HBASE_THRIFT_IP,HBASE_THRIFT_PORT);
//		
//		TProtocolFactory protocol=new TBinaryProtocol.Factory(true,true);
//		TAsyncClientManager clientManager=new TAsyncClientManager();
//		client = new THBaseService.AsyncClient(protocol,clientManager,nonblockTransport);
////		nonblockTransport.open();
//		
//		return client;
//	}
	public void close() {
		transport.close();
	}
//	public void closeAsync() {
//		nonblockTransport.close();
//	}
}

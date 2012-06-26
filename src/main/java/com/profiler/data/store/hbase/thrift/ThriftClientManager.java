package com.profiler.data.store.hbase.thrift;

import org.apache.hadoop.hbase.thrift.generated.Hbase;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.transport.TSocket;
import org.apache.thrift.transport.TTransport;


import static com.profiler.config.TomcatProfilerReceiverConfig.*;
public class ThriftClientManager {
	public ThriftClientManager() {}
	TTransport transport;
	public Hbase.Client getClient() throws Exception{
		Hbase.Client client;
		transport=new TSocket(HBASE_THRIFT_IP,HBASE_THRIFT_PORT);
		TProtocol protocol=new TBinaryProtocol(transport,true,true);
		client = new Hbase.Client(protocol);
		transport.open();
		return client;
	}
	public void close() {
		transport.close();
	}
}

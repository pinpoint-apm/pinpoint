package com.profiler.data.store.hbase.put;

import java.nio.ByteBuffer;

import org.apache.hadoop.hbase.thrift.generated.Hbase;
import org.apache.hadoop.hbase.thrift.generated.Mutation;
import org.apache.log4j.Logger;

import com.profiler.data.store.hbase.thrift.ThriftClientManager;

public abstract class AbstractPutData extends Thread {
	protected static final Logger logger = Logger.getLogger("HBasePutData");
	protected String tableName;
	public AbstractPutData(String tableName) {
		this.tableName=tableName;
	}
	public void run() {
		put(tableName);
	}
	public void put(String tableName) {
		long startTime=System.nanoTime();
		long clientConnectionTime=startTime;
		try {
			ByteBuffer tableNameBuffer=ByteBuffer.wrap(tableName.getBytes());
			ThriftClientManager manager=new ThriftClientManager();
			Hbase.Client client=manager.getClient();
			clientConnectionTime=System.nanoTime();
			writeData(tableNameBuffer,client);
			manager.close();
		} catch(Exception e) {
			e.printStackTrace();
		}
		
		long endTime=System.nanoTime();
		if(logger.isDebugEnabled()) {
			logger.debug(tableName+" clientConnection:"+(clientConnectionTime-startTime)/1000000.0
					+" put:"+(endTime-clientConnectionTime)/1000000.0
					+" Total:"+(endTime-startTime)/1000000.0+" ms elapsed.");
		}
	}
	protected Mutation getMutation(String columnNQualifier,byte[] data) {
		ByteBuffer columnByteBuffer=ByteBuffer.wrap(columnNQualifier.getBytes());
		ByteBuffer dataByteBuffer=ByteBuffer.wrap(data);
		return new Mutation(false,columnByteBuffer,dataByteBuffer);
	}
	protected abstract void writeData(ByteBuffer tableNameBuffer,Hbase.Client client) ;
}

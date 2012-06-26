package com.profiler.data.store.hbase.put2;

import java.nio.ByteBuffer;

import org.apache.hadoop.hbase.thrift2.generated.TColumnValue;
import org.apache.hadoop.hbase.thrift2.generated.THBaseService;
import org.apache.log4j.Logger;

import com.profiler.data.store.hbase.thrift.Thrift2ClientManager;
import com.profiler.util.Converter;

public abstract class AbstractPutData extends Thread {
	protected static final Logger logger = Logger.getLogger("HBasePutData");
	protected String tableName;
	private boolean isAsync=false;
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
			Thrift2ClientManager manager=new Thrift2ClientManager();
			THBaseService.Client client=manager.getClient();
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
//	protected Mutation getMutation(String columnNQualifier,byte[] data) {
//		ByteBuffer columnByteBuffer=ByteBuffer.wrap(columnNQualifier.getBytes());
//		ByteBuffer dataByteBuffer=ByteBuffer.wrap(data);
//		return new Mutation(false,columnByteBuffer,dataByteBuffer);
//	}
	protected TColumnValue getTColumnValue(String columnName,String qualifier,byte[] data) {
		TColumnValue column=new TColumnValue(
				Converter.toByteBuffer(columnName),
				Converter.toByteBuffer(qualifier),
				ByteBuffer.wrap(data));
		return column;
	}
	protected TColumnValue getTColumnValue(String columnName,String qualifier,String data) {
		return getTColumnValue(columnName,qualifier,data.getBytes());
	}
	protected abstract void writeData(ByteBuffer tableNameBuffer,THBaseService.Client client);
//	protected void writeData(ByteBuffer tableNameBuffer,THBaseService.AsyncClient asyncClient,Thrift2ClientManager manager) {};
}

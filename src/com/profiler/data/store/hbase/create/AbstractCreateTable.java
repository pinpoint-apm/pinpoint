package com.profiler.data.store.hbase.create;

import java.nio.ByteBuffer;
import java.util.List;

import org.apache.hadoop.hbase.thrift.generated.ColumnDescriptor;
import org.apache.hadoop.hbase.thrift.generated.Hbase;
import org.apache.log4j.Logger;

import com.profiler.data.store.hbase.thrift.ThriftClientManager;

public abstract class AbstractCreateTable extends Thread{
	private static final Logger logger = Logger.getLogger("HBaseTableCreate");
	String tableName=null;
	public AbstractCreateTable(String tableName) {
		this.tableName=tableName;
	}
	public void run() {
//		createTable(tableName);
		createTableWithThrift(tableName);
	}
	public void createTableWithThrift(String tableName) {
		try {
			ByteBuffer tableNameBuffer=ByteBuffer.wrap(tableName.getBytes());
			List<ColumnDescriptor> columnFamilies=getColumnList();
			
			ThriftClientManager manager=new ThriftClientManager();
			Hbase.Client client=manager.getClient();
			if(!client.isTableEnabled(tableNameBuffer)) {
				client.createTable(tableNameBuffer, columnFamilies);
			}
			manager.close();
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	protected abstract List<ColumnDescriptor> getColumnList() ;
	protected ColumnDescriptor getColumnDescriptor(String columnName) {
		ColumnDescriptor cd=new ColumnDescriptor();
		ByteBuffer cdName=ByteBuffer.wrap(columnName.getBytes());
		cd.setName(cdName);
		return cd;
	}
	protected ColumnDescriptor getColumnDescriptorWithVersion(String columnName) {
		ColumnDescriptor cd=new ColumnDescriptor();
		ByteBuffer cdName=ByteBuffer.wrap(columnName.getBytes());
		cd.setName(cdName);
		cd.setMaxVersionsIsSet(true);
		cd.setMaxVersions(20);
		return cd;
	}
//	public void createTable(String tableName) {
//		try {
////			long time1=System.nanoTime();
//			Configuration config = HBaseConfiguration.create();
////			long time2=System.nanoTime();
//			HTableDescriptor tableDesc=new HTableDescriptor();
//			tableDesc.setName(tableName.getBytes());
//			
//			HBaseAdmin admin = new HBaseAdmin(config);
////			long time3=System.nanoTime();
//			boolean isTableAvailable=admin.isTableAvailable(tableName);
//			if(!isTableAvailable) {
////				long time4=System.nanoTime();
//				admin.createTable(tableDesc);
////				long time5=System.nanoTime();
//				admin.disableTable(tableName);
////				long time6=System.nanoTime();
//				addColumns(tableName,admin);
////				long time7=System.nanoTime();
//				admin.enableTable(tableName);
//				logger.debug(tableName+" Table is created.");
////				long time8=System.nanoTime();
////				System.out.println(tableName);
////				System.out.print(" 1 "+(time2-time1)/1000000.0);
////				System.out.print(" 2 "+(time3-time2)/1000000.0);
////				System.out.print(" 3 "+(time4-time3)/1000000.0);
////				System.out.print(" 4 "+(time5-time4)/1000000.0);
////				System.out.print(" 5 "+(time6-time5)/1000000.0);
////				System.out.print(" 6 "+(time7-time6)/1000000.0);
////				System.out.print(" 7 "+(time8-time7)/1000000.0);
////				System.out.println();
//			} else {
//				logger.debug(tableName+" Table is already exist.");
//			}
//			admin.close();
//			
//		} catch (Exception e) {
//			e.printStackTrace();
//			logger.error(e.getMessage());
//		}
//	}
//	protected abstract void addColumns(String tableName,HBaseAdmin admin) throws Exception;
}

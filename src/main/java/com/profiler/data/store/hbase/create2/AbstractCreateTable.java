package com.profiler.data.store.hbase.create2;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.HColumnDescriptor;
import org.apache.hadoop.hbase.HTableDescriptor;
import org.apache.hadoop.hbase.client.HBaseAdmin;
import org.apache.log4j.Logger;

public abstract class AbstractCreateTable extends Thread{
	private static final Logger logger = Logger.getLogger("HBaseTableCreate");
	String tableName=null;
	public AbstractCreateTable(String tableName) {
		this.tableName=tableName;
	}
	public void run() {
		createTable(tableName);
//		createTableWithThrift(tableName);
	}
	public void createTable(String tableName) {
		try {
//			long time1=System.nanoTime();
			Configuration config = HBaseConfiguration.create();
//			long time2=System.nanoTime();
			
			HBaseAdmin admin = new HBaseAdmin(config);
//			long time3=System.nanoTime();
			boolean isTableAvailable=admin.isTableAvailable(tableName);
			if(!isTableAvailable) {
//				long time4=System.nanoTime();
				HTableDescriptor tableDesc=getTableDescriptor(tableName);//new HTableDescriptor();
				tableDesc.setName(tableName.getBytes());
				admin.createTable(tableDesc);
//				long time5=System.nanoTime();
//				admin.disableTable(tableName);
//				long time6=System.nanoTime();
//				addColumns(tableName,admin);
//				long time7=System.nanoTime();
//				admin.enableTable(tableName);
				logger.debug(tableName+" Table is created.");
//				long time8=System.nanoTime();
//				System.out.println(tableName);
//				System.out.print(" 1 "+(time2-time1)/1000000.0);
//				System.out.print(" 2 "+(time3-time2)/1000000.0);
//				System.out.print(" 3 "+(time4-time3)/1000000.0);
//				System.out.print(" 4 "+(time5-time4)/1000000.0);
//				System.out.print(" 5 "+(time6-time5)/1000000.0);
//				System.out.print(" 6 "+(time7-time6)/1000000.0);
//				System.out.print(" 7 "+(time8-time7)/1000000.0);
//				System.out.println();
			} else {
				logger.debug(tableName+" Table is already exist.");
			}
			admin.close();
			
		} catch (Exception e) {
			e.printStackTrace();
			logger.error(e.getMessage());
		}
	}
	protected void addColumn(String tableName, HBaseAdmin admin,String columnName) throws Exception{
		HColumnDescriptor columnDesc = new HColumnDescriptor(columnName);
		admin.addColumn(tableName, columnDesc);
	}
//	protected abstract void addColumns(String tableName,HBaseAdmin admin) throws Exception;
	protected abstract HTableDescriptor getTableDescriptor(String tableName) throws Exception;
}

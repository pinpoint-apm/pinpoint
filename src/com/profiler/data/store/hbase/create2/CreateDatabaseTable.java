package com.profiler.data.store.hbase.create2;

import static com.profiler.config.TomcatProfilerReceiverConstant.HBASE_DATABASE_COLUMN_CONNECTION_URL;
import static com.profiler.config.TomcatProfilerReceiverConstant.HBASE_DATABASE_COLUMN_DB_ROW_NAMES;
import static com.profiler.config.TomcatProfilerReceiverConstant.HBASE_DATABASE_COLUMN_QUERY_ROW_NAMES;
import static com.profiler.config.TomcatProfilerReceiverConstant.HBASE_DATABASE_COLUMN_QUERY_STRING;

import org.apache.hadoop.hbase.HColumnDescriptor;
import org.apache.hadoop.hbase.HTableDescriptor;
import org.apache.hadoop.hbase.client.HBaseAdmin;
public class CreateDatabaseTable extends AbstractCreateTable {
	public CreateDatabaseTable(String tableName) {
		super(tableName);
	}
//	@Override
//	protected List<ColumnDescriptor> getColumnList() {
//		List<ColumnDescriptor> columnList=new ArrayList<ColumnDescriptor>();
//		columnList.add(getColumnDescriptor(HBASE_DATABASE_COLUMN_CONNECTION_URL));
//		columnList.add(getColumnDescriptor(HBASE_DATABASE_COLUMN_QUERY_STRING));
//		columnList.add(getColumnDescriptor(HBASE_DATABASE_COLUMN_DB_ROW_NAMES));
//		columnList.add(getColumnDescriptor(HBASE_DATABASE_COLUMN_QUERY_ROW_NAMES));
//		return columnList;
//	}
//	protected void addColumns(String tableName, HBaseAdmin admin)
//			throws Exception {
//		HTableDescriptor tableDesc=getTableDescriptor(tableName);//new HTableDescriptor();
//		tableDesc.setName(tableName.getBytes());
//		
//		addColumn(tableName,admin,HBASE_DATABASE_COLUMN_CONNECTION_URL);
//		addColumn(tableName,admin,HBASE_DATABASE_COLUMN_QUERY_STRING);
//		addColumn(tableName,admin,HBASE_DATABASE_COLUMN_DB_ROW_NAMES);
//		addColumn(tableName,admin,HBASE_DATABASE_COLUMN_QUERY_ROW_NAMES);
//	}
	@Override
	protected HTableDescriptor getTableDescriptor(String tableName)
			throws Exception {
		HTableDescriptor tableDesc=new HTableDescriptor(tableName);
		tableDesc.setName(tableName.getBytes());
		tableDesc.addFamily(new HColumnDescriptor(HBASE_DATABASE_COLUMN_CONNECTION_URL));
		tableDesc.addFamily(new HColumnDescriptor(HBASE_DATABASE_COLUMN_QUERY_STRING));
		tableDesc.addFamily(new HColumnDescriptor(HBASE_DATABASE_COLUMN_DB_ROW_NAMES));
		tableDesc.addFamily(new HColumnDescriptor(HBASE_DATABASE_COLUMN_QUERY_ROW_NAMES));
		return tableDesc;
	}
	
}

package com.profiler.data.store.hbase.create2;

import static com.profiler.config.TomcatProfilerReceiverConstant.HBASE_DATABASE_COLUMN_CONNECTION_URL;
import static com.profiler.config.TomcatProfilerReceiverConstant.HBASE_DATABASE_COLUMN_DB_ROW_NAMES;
import static com.profiler.config.TomcatProfilerReceiverConstant.HBASE_DATABASE_COLUMN_QUERY_ROW_NAMES;
import static com.profiler.config.TomcatProfilerReceiverConstant.HBASE_DATABASE_COLUMN_QUERY_STRING;
import static com.profiler.config.TomcatProfilerReceiverConstant.HBASE_REQUEST_COLUMN_CLIENT_IP;
import static com.profiler.config.TomcatProfilerReceiverConstant.HBASE_REQUEST_COLUMN_ELAPSED_TIME;
import static com.profiler.config.TomcatProfilerReceiverConstant.HBASE_REQUEST_COLUMN_REQUEST_DATA;
import static com.profiler.config.TomcatProfilerReceiverConstant.HBASE_REQUEST_COLUMN_REQUEST_PARAMS;
import static com.profiler.config.TomcatProfilerReceiverConstant.HBASE_REQUEST_COLUMN_REQUEST_TIME;
import static com.profiler.config.TomcatProfilerReceiverConstant.HBASE_REQUEST_COLUMN_REQUEST_URL;
import static com.profiler.config.TomcatProfilerReceiverConstant.HBASE_REQUEST_COLUMN_RESPONSE_TIME;

import org.apache.hadoop.hbase.HColumnDescriptor;
import org.apache.hadoop.hbase.HTableDescriptor;
import org.apache.hadoop.hbase.client.HBaseAdmin;
public class CreateRequestTable extends AbstractCreateTable {
	public CreateRequestTable(String tableName) {
		super(tableName);
	}
//	@Override
//	protected List<ColumnDescriptor> getColumnList() {
//		List<ColumnDescriptor> columnList=new ArrayList<ColumnDescriptor>();
//		columnList.add(getColumnDescriptor(HBASE_REQUEST_COLUMN_REQUEST_TIME));
//		columnList.add(getColumnDescriptor(HBASE_REQUEST_COLUMN_RESPONSE_TIME));
//		columnList.add(getColumnDescriptor(HBASE_REQUEST_COLUMN_ELAPSED_TIME));
//		columnList.add(getColumnDescriptor(HBASE_REQUEST_COLUMN_REQUEST_URL));
//		columnList.add(getColumnDescriptor(HBASE_REQUEST_COLUMN_CLIENT_IP));
//		columnList.add(getColumnDescriptor(HBASE_REQUEST_COLUMN_REQUEST_DATA));
//		columnList.add(getColumnDescriptor(HBASE_REQUEST_COLUMN_REQUEST_PARAMS));
//		return columnList;
//	}
//	@Override
//	protected void addColumns(String tableName, HBaseAdmin admin)
//			throws Exception {
//		addColumn(tableName,admin,HBASE_REQUEST_COLUMN_REQUEST_TIME);
//		addColumn(tableName,admin,HBASE_REQUEST_COLUMN_RESPONSE_TIME);
//		addColumn(tableName,admin,HBASE_REQUEST_COLUMN_ELAPSED_TIME);
//		addColumn(tableName,admin,HBASE_REQUEST_COLUMN_REQUEST_URL);
//		addColumn(tableName,admin,HBASE_REQUEST_COLUMN_CLIENT_IP);
//		addColumn(tableName,admin,HBASE_REQUEST_COLUMN_REQUEST_DATA);
//		addColumn(tableName,admin,HBASE_REQUEST_COLUMN_REQUEST_PARAMS);
//	}
	
	@Override
	protected HTableDescriptor getTableDescriptor(String tableName)
			throws Exception {
		HTableDescriptor tableDesc=new HTableDescriptor(tableName);
		tableDesc.setName(tableName.getBytes());
		tableDesc.addFamily(new HColumnDescriptor(HBASE_REQUEST_COLUMN_REQUEST_TIME));
		tableDesc.addFamily(new HColumnDescriptor(HBASE_REQUEST_COLUMN_RESPONSE_TIME));
		tableDesc.addFamily(new HColumnDescriptor(HBASE_REQUEST_COLUMN_ELAPSED_TIME));
		tableDesc.addFamily(new HColumnDescriptor(HBASE_REQUEST_COLUMN_REQUEST_URL));
		tableDesc.addFamily(new HColumnDescriptor(HBASE_REQUEST_COLUMN_CLIENT_IP));
		tableDesc.addFamily(new HColumnDescriptor(HBASE_REQUEST_COLUMN_REQUEST_DATA));
		tableDesc.addFamily(new HColumnDescriptor(HBASE_REQUEST_COLUMN_REQUEST_PARAMS));
		return tableDesc;
	}
}

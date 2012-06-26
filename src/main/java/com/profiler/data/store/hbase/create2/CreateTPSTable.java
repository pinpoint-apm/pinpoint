package com.profiler.data.store.hbase.create2;

import static com.profiler.config.TomcatProfilerReceiverConstant.HBASE_REQUEST_COLUMN_CLIENT_IP;
import static com.profiler.config.TomcatProfilerReceiverConstant.HBASE_REQUEST_COLUMN_ELAPSED_TIME;
import static com.profiler.config.TomcatProfilerReceiverConstant.HBASE_REQUEST_COLUMN_REQUEST_DATA;
import static com.profiler.config.TomcatProfilerReceiverConstant.HBASE_REQUEST_COLUMN_REQUEST_PARAMS;
import static com.profiler.config.TomcatProfilerReceiverConstant.HBASE_REQUEST_COLUMN_REQUEST_TIME;
import static com.profiler.config.TomcatProfilerReceiverConstant.HBASE_REQUEST_COLUMN_REQUEST_URL;
import static com.profiler.config.TomcatProfilerReceiverConstant.HBASE_REQUEST_COLUMN_RESPONSE_TIME;
import static com.profiler.config.TomcatProfilerReceiverConstant.HBASE_TPS_COLUMN_REQUEST_TPS;
import static com.profiler.config.TomcatProfilerReceiverConstant.HBASE_TPS_COLUMN_RESPONSE_TPS;

import org.apache.hadoop.hbase.HColumnDescriptor;
import org.apache.hadoop.hbase.HTableDescriptor;
import org.apache.hadoop.hbase.client.HBaseAdmin;

public class CreateTPSTable extends AbstractCreateTable {
	public CreateTPSTable(String tableName) {
		super(tableName);
	}
//	@Override
//	protected List<ColumnDescriptor> getColumnList() {
//		List<ColumnDescriptor> columnList=new ArrayList<ColumnDescriptor>();
//		columnList.add(getColumnDescriptor(HBASE_TPS_COLUMN_REQUEST_TPS));
//		columnList.add(getColumnDescriptor(HBASE_TPS_COLUMN_RESPONSE_TPS));
//		return columnList;
//	}
//	@Override
//	protected void addColumns(String tableName, HBaseAdmin admin)
//			throws Exception {
//		addColumn(tableName,admin,HBASE_TPS_COLUMN_REQUEST_TPS);
//		addColumn(tableName,admin,HBASE_TPS_COLUMN_RESPONSE_TPS);
//	}
	@Override
	protected HTableDescriptor getTableDescriptor(String tableName)
			throws Exception {
		HTableDescriptor tableDesc=new HTableDescriptor(tableName);
		tableDesc.setName(tableName.getBytes());
		tableDesc.addFamily(new HColumnDescriptor(HBASE_TPS_COLUMN_REQUEST_TPS));
		tableDesc.addFamily(new HColumnDescriptor(HBASE_TPS_COLUMN_RESPONSE_TPS));
		return tableDesc;
	}
}

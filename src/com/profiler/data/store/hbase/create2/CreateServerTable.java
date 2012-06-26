package com.profiler.data.store.hbase.create2;

import static com.profiler.config.TomcatProfilerReceiverConstant.HBASE_REQUEST_COLUMN_CLIENT_IP;
import static com.profiler.config.TomcatProfilerReceiverConstant.HBASE_REQUEST_COLUMN_ELAPSED_TIME;
import static com.profiler.config.TomcatProfilerReceiverConstant.HBASE_REQUEST_COLUMN_REQUEST_DATA;
import static com.profiler.config.TomcatProfilerReceiverConstant.HBASE_REQUEST_COLUMN_REQUEST_PARAMS;
import static com.profiler.config.TomcatProfilerReceiverConstant.HBASE_REQUEST_COLUMN_REQUEST_TIME;
import static com.profiler.config.TomcatProfilerReceiverConstant.HBASE_REQUEST_COLUMN_REQUEST_URL;
import static com.profiler.config.TomcatProfilerReceiverConstant.HBASE_REQUEST_COLUMN_RESPONSE_TIME;
import static com.profiler.config.TomcatProfilerReceiverConstant.HBASE_SERVER_COLUMN_INSTANCE_NAME;
import static com.profiler.config.TomcatProfilerReceiverConstant.HBASE_SERVER_COLUMN_IP;
import static com.profiler.config.TomcatProfilerReceiverConstant.HBASE_SERVER_COLUMN_IS_RUNNING;
import static com.profiler.config.TomcatProfilerReceiverConstant.HBASE_SERVER_COLUMN_PORTS;
import static com.profiler.config.TomcatProfilerReceiverConstant.HBASE_SERVER_COLUMN_ROW_NAMES;
import static com.profiler.config.TomcatProfilerReceiverConstant.HBASE_SERVER_COLUMN_SERVER_GROUP_NAME;
import static com.profiler.config.TomcatProfilerReceiverConstant.HBASE_SERVER_COLUMN_SERVICE_NAME;

import org.apache.hadoop.hbase.HColumnDescriptor;
import org.apache.hadoop.hbase.HTableDescriptor;
import org.apache.hadoop.hbase.client.HBaseAdmin;
public class CreateServerTable extends AbstractCreateTable {
	public CreateServerTable(String tableName) {
		super(tableName);
	}
//	@Override
//	protected void addColumns(String tableName, HBaseAdmin admin)
//			throws Exception {
//		addColumn(tableName,admin,HBASE_SERVER_COLUMN_ROW_NAMES);
//		addColumn(tableName,admin,HBASE_SERVER_COLUMN_IP);
//		addColumn(tableName,admin,HBASE_SERVER_COLUMN_PORTS);
//		addColumn(tableName,admin,HBASE_SERVER_COLUMN_IS_RUNNING);
//		addColumn(tableName,admin,HBASE_SERVER_COLUMN_SERVICE_NAME);
//		addColumn(tableName,admin,HBASE_SERVER_COLUMN_SERVER_GROUP_NAME);
//		addColumn(tableName,admin,HBASE_SERVER_COLUMN_INSTANCE_NAME);
//	}
	@Override
	protected HTableDescriptor getTableDescriptor(String tableName)
			throws Exception {
		HTableDescriptor tableDesc=new HTableDescriptor(tableName);
		tableDesc.setName(tableName.getBytes());
		tableDesc.addFamily(new HColumnDescriptor(HBASE_SERVER_COLUMN_ROW_NAMES));
		tableDesc.addFamily(new HColumnDescriptor(HBASE_SERVER_COLUMN_IP));
		tableDesc.addFamily(new HColumnDescriptor(HBASE_SERVER_COLUMN_PORTS));
		tableDesc.addFamily(new HColumnDescriptor(HBASE_SERVER_COLUMN_IS_RUNNING));
		tableDesc.addFamily(new HColumnDescriptor(HBASE_SERVER_COLUMN_SERVICE_NAME));
		tableDesc.addFamily(new HColumnDescriptor(HBASE_SERVER_COLUMN_SERVER_GROUP_NAME));
		tableDesc.addFamily(new HColumnDescriptor(HBASE_SERVER_COLUMN_INSTANCE_NAME));
		return tableDesc;
	}
}

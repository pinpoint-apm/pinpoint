package com.profiler.data.store.hbase.create;

import static com.profiler.config.TomcatProfilerReceiverConstant.HBASE_REQUEST_COLUMN_CLIENT_IP;
import static com.profiler.config.TomcatProfilerReceiverConstant.HBASE_REQUEST_COLUMN_ELAPSED_TIME;
import static com.profiler.config.TomcatProfilerReceiverConstant.HBASE_REQUEST_COLUMN_REQUEST_TIME;
import static com.profiler.config.TomcatProfilerReceiverConstant.HBASE_REQUEST_COLUMN_REQUEST_URL;
import static com.profiler.config.TomcatProfilerReceiverConstant.HBASE_REQUEST_COLUMN_RESPONSE_TIME;
import static com.profiler.config.TomcatProfilerReceiverConstant.HBASE_REQUEST_COLUMN_REQUEST_DATA;
import static com.profiler.config.TomcatProfilerReceiverConstant.HBASE_REQUEST_COLUMN_REQUEST_PARAMS;
import java.util.ArrayList;
import java.util.List;

import org.apache.hadoop.hbase.thrift.generated.ColumnDescriptor;
public class CreateRequestTable extends AbstractCreateTable {
	public CreateRequestTable(String tableName) {
		super(tableName);
	}
	@Override
	protected List<ColumnDescriptor> getColumnList() {
		List<ColumnDescriptor> columnList=new ArrayList<ColumnDescriptor>();
		columnList.add(getColumnDescriptor(HBASE_REQUEST_COLUMN_REQUEST_TIME));
		columnList.add(getColumnDescriptor(HBASE_REQUEST_COLUMN_RESPONSE_TIME));
		columnList.add(getColumnDescriptor(HBASE_REQUEST_COLUMN_ELAPSED_TIME));
		columnList.add(getColumnDescriptor(HBASE_REQUEST_COLUMN_REQUEST_URL));
		columnList.add(getColumnDescriptor(HBASE_REQUEST_COLUMN_CLIENT_IP));
		columnList.add(getColumnDescriptor(HBASE_REQUEST_COLUMN_REQUEST_DATA));
		columnList.add(getColumnDescriptor(HBASE_REQUEST_COLUMN_REQUEST_PARAMS));
		return columnList;
	}
}

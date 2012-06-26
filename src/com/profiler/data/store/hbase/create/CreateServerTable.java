package com.profiler.data.store.hbase.create;

import static com.profiler.config.TomcatProfilerReceiverConstant.HBASE_SERVER_COLUMN_INSTANCE_NAME;
import static com.profiler.config.TomcatProfilerReceiverConstant.HBASE_SERVER_COLUMN_IP;
import static com.profiler.config.TomcatProfilerReceiverConstant.HBASE_SERVER_COLUMN_IS_RUNNING;
import static com.profiler.config.TomcatProfilerReceiverConstant.HBASE_SERVER_COLUMN_PORTS;
import static com.profiler.config.TomcatProfilerReceiverConstant.HBASE_SERVER_COLUMN_ROW_NAMES;
import static com.profiler.config.TomcatProfilerReceiverConstant.HBASE_SERVER_COLUMN_SERVER_GROUP_NAME;
import static com.profiler.config.TomcatProfilerReceiverConstant.HBASE_SERVER_COLUMN_SERVICE_NAME;

import java.util.ArrayList;
import java.util.List;

import org.apache.hadoop.hbase.thrift.generated.ColumnDescriptor;
public class CreateServerTable extends AbstractCreateTable {
	public CreateServerTable(String tableName) {
		super(tableName);
	}
	@Override
	protected List<ColumnDescriptor> getColumnList() {
		List<ColumnDescriptor> columnList=new ArrayList<ColumnDescriptor>();
		columnList.add(getColumnDescriptorWithVersion(HBASE_SERVER_COLUMN_ROW_NAMES));
		columnList.add(getColumnDescriptorWithVersion(HBASE_SERVER_COLUMN_IP));
		columnList.add(getColumnDescriptorWithVersion(HBASE_SERVER_COLUMN_PORTS));
		columnList.add(getColumnDescriptorWithVersion(HBASE_SERVER_COLUMN_IS_RUNNING));
		columnList.add(getColumnDescriptorWithVersion(HBASE_SERVER_COLUMN_SERVICE_NAME));
		columnList.add(getColumnDescriptorWithVersion(HBASE_SERVER_COLUMN_SERVER_GROUP_NAME));
		columnList.add(getColumnDescriptorWithVersion(HBASE_SERVER_COLUMN_INSTANCE_NAME));
		return columnList;
	}

}

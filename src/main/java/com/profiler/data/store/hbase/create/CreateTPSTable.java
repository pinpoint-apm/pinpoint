package com.profiler.data.store.hbase.create;

import static com.profiler.config.TomcatProfilerReceiverConstant.HBASE_TPS_COLUMN_REQUEST_TPS;
import static com.profiler.config.TomcatProfilerReceiverConstant.HBASE_TPS_COLUMN_RESPONSE_TPS;

import java.util.ArrayList;
import java.util.List;

import org.apache.hadoop.hbase.thrift.generated.ColumnDescriptor;

public class CreateTPSTable extends AbstractCreateTable {
	public CreateTPSTable(String tableName) {
		super(tableName);
	}
	@Override
	protected List<ColumnDescriptor> getColumnList() {
		List<ColumnDescriptor> columnList=new ArrayList<ColumnDescriptor>();
		columnList.add(getColumnDescriptor(HBASE_TPS_COLUMN_REQUEST_TPS));
		columnList.add(getColumnDescriptor(HBASE_TPS_COLUMN_RESPONSE_TPS));
		return columnList;
	}
}

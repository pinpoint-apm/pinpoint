package com.profiler.data.store.hbase.create;

import static com.profiler.config.TomcatProfilerReceiverConstant.HBASE_DATABASE_COLUMN_CONNECTION_URL;
import static com.profiler.config.TomcatProfilerReceiverConstant.HBASE_DATABASE_COLUMN_QUERY_STRING;

import java.util.ArrayList;
import java.util.List;

import org.apache.hadoop.hbase.thrift.generated.ColumnDescriptor;
import static com.profiler.config.TomcatProfilerReceiverConstant.HBASE_DATABASE_COLUMN_DB_ROW_NAMES;
import static com.profiler.config.TomcatProfilerReceiverConstant.HBASE_DATABASE_COLUMN_QUERY_ROW_NAMES;
public class CreateDatabaseTable extends AbstractCreateTable {
	public CreateDatabaseTable(String tableName) {
		super(tableName);
	}
	@Override
	protected List<ColumnDescriptor> getColumnList() {
		List<ColumnDescriptor> columnList=new ArrayList<ColumnDescriptor>();
		columnList.add(getColumnDescriptor(HBASE_DATABASE_COLUMN_CONNECTION_URL));
		columnList.add(getColumnDescriptor(HBASE_DATABASE_COLUMN_QUERY_STRING));
		columnList.add(getColumnDescriptor(HBASE_DATABASE_COLUMN_DB_ROW_NAMES));
		columnList.add(getColumnDescriptor(HBASE_DATABASE_COLUMN_QUERY_ROW_NAMES));
		return columnList;
	}
}

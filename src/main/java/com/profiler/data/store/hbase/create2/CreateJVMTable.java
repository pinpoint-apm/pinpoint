package com.profiler.data.store.hbase.create2;

import static com.profiler.config.TomcatProfilerReceiverConstant.HBASE_DATABASE_COLUMN_CONNECTION_URL;
import static com.profiler.config.TomcatProfilerReceiverConstant.HBASE_DATABASE_COLUMN_DB_ROW_NAMES;
import static com.profiler.config.TomcatProfilerReceiverConstant.HBASE_DATABASE_COLUMN_QUERY_ROW_NAMES;
import static com.profiler.config.TomcatProfilerReceiverConstant.HBASE_DATABASE_COLUMN_QUERY_STRING;
import static com.profiler.config.TomcatProfilerReceiverConstant.HBASE_JVM_COLUMN_ACTIVE_THREAD_COUNT;
import static com.profiler.config.TomcatProfilerReceiverConstant.HBASE_JVM_COLUMN_GC1_COUNT;
import static com.profiler.config.TomcatProfilerReceiverConstant.HBASE_JVM_COLUMN_GC1_TIME;
import static com.profiler.config.TomcatProfilerReceiverConstant.HBASE_JVM_COLUMN_GC2_COUNT;
import static com.profiler.config.TomcatProfilerReceiverConstant.HBASE_JVM_COLUMN_GC2_TIME;
import static com.profiler.config.TomcatProfilerReceiverConstant.HBASE_JVM_COLUMN_HEAP_COMMITTED;
import static com.profiler.config.TomcatProfilerReceiverConstant.HBASE_JVM_COLUMN_HEAP_USED;
import static com.profiler.config.TomcatProfilerReceiverConstant.HBASE_JVM_COLUMN_NON_HEAP_COMMITTED;
import static com.profiler.config.TomcatProfilerReceiverConstant.HBASE_JVM_COLUMN_NON_HEAP_USED;
import static com.profiler.config.TomcatProfilerReceiverConstant.HBASE_JVM_COLUMN_PROCESS_CPU_TIME;

import org.apache.hadoop.hbase.HColumnDescriptor;
import org.apache.hadoop.hbase.HTableDescriptor;
import org.apache.hadoop.hbase.client.HBaseAdmin;

public class CreateJVMTable extends AbstractCreateTable {
	public CreateJVMTable(String tableName) {
		super(tableName);
	}
//	@Override
//	protected List<ColumnDescriptor> getColumnList() {
//		List<ColumnDescriptor> columnList=new ArrayList<ColumnDescriptor>();
//		columnList.add(getColumnDescriptor(HBASE_JVM_COLUMN_ACTIVE_THREAD_COUNT));
//		columnList.add(getColumnDescriptor(HBASE_JVM_COLUMN_GC1_COUNT));
//		columnList.add(getColumnDescriptor(HBASE_JVM_COLUMN_GC1_TIME));
//		columnList.add(getColumnDescriptor(HBASE_JVM_COLUMN_GC2_COUNT));
//		columnList.add(getColumnDescriptor(HBASE_JVM_COLUMN_GC2_TIME));
//		columnList.add(getColumnDescriptor(HBASE_JVM_COLUMN_HEAP_USED));
//		columnList.add(getColumnDescriptor(HBASE_JVM_COLUMN_HEAP_COMMITTED));
//		columnList.add(getColumnDescriptor(HBASE_JVM_COLUMN_NON_HEAP_USED));
//		columnList.add(getColumnDescriptor(HBASE_JVM_COLUMN_NON_HEAP_COMMITTED));
//		columnList.add(getColumnDescriptor(HBASE_JVM_COLUMN_PROCESS_CPU_TIME));
//		return columnList;
//	}
//	@Override
//	protected void addColumns(String tableName, HBaseAdmin admin)
//			throws Exception {
//		addColumn(tableName,admin,HBASE_JVM_COLUMN_ACTIVE_THREAD_COUNT);
//		addColumn(tableName,admin,HBASE_JVM_COLUMN_GC1_COUNT);
//		addColumn(tableName,admin,HBASE_JVM_COLUMN_GC1_TIME);
//		addColumn(tableName,admin,HBASE_JVM_COLUMN_GC2_COUNT);
//		addColumn(tableName,admin,HBASE_JVM_COLUMN_GC2_TIME);
//		addColumn(tableName,admin,HBASE_JVM_COLUMN_HEAP_USED);
//		addColumn(tableName,admin,HBASE_JVM_COLUMN_HEAP_COMMITTED);
//		addColumn(tableName,admin,HBASE_JVM_COLUMN_NON_HEAP_USED);
//		addColumn(tableName,admin,HBASE_JVM_COLUMN_NON_HEAP_COMMITTED);
//		addColumn(tableName,admin,HBASE_JVM_COLUMN_PROCESS_CPU_TIME);
//	}
	@Override
	protected HTableDescriptor getTableDescriptor(String tableName)
			throws Exception {
		HTableDescriptor tableDesc=new HTableDescriptor(tableName);
		tableDesc.setName(tableName.getBytes());
		tableDesc.addFamily(new HColumnDescriptor(HBASE_JVM_COLUMN_ACTIVE_THREAD_COUNT));
		tableDesc.addFamily(new HColumnDescriptor(HBASE_JVM_COLUMN_GC1_COUNT));
		tableDesc.addFamily(new HColumnDescriptor(HBASE_JVM_COLUMN_GC1_TIME));
		tableDesc.addFamily(new HColumnDescriptor(HBASE_JVM_COLUMN_GC2_COUNT));
		tableDesc.addFamily(new HColumnDescriptor(HBASE_JVM_COLUMN_GC2_TIME));
		tableDesc.addFamily(new HColumnDescriptor(HBASE_JVM_COLUMN_HEAP_USED));
		tableDesc.addFamily(new HColumnDescriptor(HBASE_JVM_COLUMN_HEAP_COMMITTED));
		tableDesc.addFamily(new HColumnDescriptor(HBASE_JVM_COLUMN_NON_HEAP_USED));
		tableDesc.addFamily(new HColumnDescriptor(HBASE_JVM_COLUMN_NON_HEAP_COMMITTED));
		tableDesc.addFamily(new HColumnDescriptor(HBASE_JVM_COLUMN_PROCESS_CPU_TIME));
		return tableDesc;
	}
}

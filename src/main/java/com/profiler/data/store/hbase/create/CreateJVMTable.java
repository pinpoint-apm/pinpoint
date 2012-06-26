package com.profiler.data.store.hbase.create;

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

import java.util.ArrayList;
import java.util.List;

import org.apache.hadoop.hbase.thrift.generated.ColumnDescriptor;

public class CreateJVMTable extends AbstractCreateTable {
	public CreateJVMTable(String tableName) {
		super(tableName);
	}
	@Override
	protected List<ColumnDescriptor> getColumnList() {
		List<ColumnDescriptor> columnList=new ArrayList<ColumnDescriptor>();
		columnList.add(getColumnDescriptor(HBASE_JVM_COLUMN_ACTIVE_THREAD_COUNT));
		columnList.add(getColumnDescriptor(HBASE_JVM_COLUMN_GC1_COUNT));
		columnList.add(getColumnDescriptor(HBASE_JVM_COLUMN_GC1_TIME));
		columnList.add(getColumnDescriptor(HBASE_JVM_COLUMN_GC2_COUNT));
		columnList.add(getColumnDescriptor(HBASE_JVM_COLUMN_GC2_TIME));
		columnList.add(getColumnDescriptor(HBASE_JVM_COLUMN_HEAP_USED));
		columnList.add(getColumnDescriptor(HBASE_JVM_COLUMN_HEAP_COMMITTED));
		columnList.add(getColumnDescriptor(HBASE_JVM_COLUMN_NON_HEAP_USED));
		columnList.add(getColumnDescriptor(HBASE_JVM_COLUMN_NON_HEAP_COMMITTED));
		columnList.add(getColumnDescriptor(HBASE_JVM_COLUMN_PROCESS_CPU_TIME));
		return columnList;
	}
}

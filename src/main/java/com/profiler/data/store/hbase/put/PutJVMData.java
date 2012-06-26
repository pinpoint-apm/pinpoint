package com.profiler.data.store.hbase.put;

import static com.profiler.config.TomcatProfilerReceiverConstant.DATE_FORMAT_YMD;
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

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.hadoop.hbase.thrift.generated.Hbase;
import org.apache.hadoop.hbase.thrift.generated.IOError;
import org.apache.hadoop.hbase.thrift.generated.Mutation;
import org.apache.hadoop.hbase.util.Bytes;

import com.profiler.dto.JVMInfoThriftDTO;
public class PutJVMData extends AbstractPutData{
	JVMInfoThriftDTO dto=null;
	public PutJVMData(String tableName,JVMInfoThriftDTO dto) {
		super(tableName);
		this.dto=dto;
	}
	public void writeData(ByteBuffer tableNameBuffer,Hbase.Client client) {
		try {
			long timestamp=dto.getDataTime();
			String rowName=DATE_FORMAT_YMD.format(new Date(timestamp));
			ByteBuffer row=ByteBuffer.wrap(rowName.getBytes());
			List<Mutation> mutations = new ArrayList<Mutation>(); 
//			mutations.add(getMutation(HBASE_JVM_COLUMN_ACTIVE_THREAD_COUNT+timestamp,Bytes.toBytes(dto.getActiveThreadCount())));
//			mutations.add(getMutation(HBASE_JVM_COLUMN_GC1_COUNT+timestamp,Bytes.toBytes(dto.getGc1Count())));
//			mutations.add(getMutation(HBASE_JVM_COLUMN_GC1_TIME+timestamp,Bytes.toBytes(dto.getGc1Time())));
//			mutations.add(getMutation(HBASE_JVM_COLUMN_GC1_TIME+timestamp,Bytes.toBytes(dto.getGc2Count())));
//			mutations.add(getMutation(HBASE_JVM_COLUMN_GC2_TIME+timestamp,Bytes.toBytes(dto.getGc2Time())));
//			mutations.add(getMutation(HBASE_JVM_COLUMN_HEAP_USED+timestamp,Bytes.toBytes(dto.getHeapUsed())));
//			mutations.add(getMutation(HBASE_JVM_COLUMN_HEAP_COMMITTED+timestamp,Bytes.toBytes(dto.getHeapCommitted())));
//			mutations.add(getMutation(HBASE_JVM_COLUMN_NON_HEAP_USED+timestamp,Bytes.toBytes(dto.getNonHeapUsed())));
//			mutations.add(getMutation(HBASE_JVM_COLUMN_NON_HEAP_COMMITTED+timestamp,Bytes.toBytes(dto.getNonHeapCommitted())));
//			mutations.add(getMutation(HBASE_JVM_COLUMN_PROCESS_CPU_TIME+timestamp,Bytes.toBytes(dto.getProcessCPUTime())));
			
			mutations.add(getMutation(HBASE_JVM_COLUMN_ACTIVE_THREAD_COUNT+timestamp,(dto.getActiveThreadCount()+"").getBytes()));
			mutations.add(getMutation(HBASE_JVM_COLUMN_GC1_COUNT+timestamp,(dto.getGc1Count()+"").getBytes()));
			mutations.add(getMutation(HBASE_JVM_COLUMN_GC1_TIME+timestamp,(dto.getGc1Time()+"").getBytes()));
			mutations.add(getMutation(HBASE_JVM_COLUMN_GC2_COUNT+timestamp,(dto.getGc2Count()+"").getBytes()));
			mutations.add(getMutation(HBASE_JVM_COLUMN_GC2_TIME+timestamp,(dto.getGc2Time()+"").getBytes()));
			mutations.add(getMutation(HBASE_JVM_COLUMN_HEAP_USED+timestamp,(dto.getHeapUsed()+"").getBytes()));
			mutations.add(getMutation(HBASE_JVM_COLUMN_HEAP_COMMITTED+timestamp,(dto.getHeapCommitted()+"").getBytes()));
			mutations.add(getMutation(HBASE_JVM_COLUMN_NON_HEAP_USED+timestamp,(dto.getNonHeapUsed()+"").getBytes()));
			mutations.add(getMutation(HBASE_JVM_COLUMN_NON_HEAP_COMMITTED+timestamp,(dto.getNonHeapCommitted()+"").getBytes()));
			mutations.add(getMutation(HBASE_JVM_COLUMN_PROCESS_CPU_TIME+timestamp,(dto.getProcessCPUTime()+"").getBytes()));
			
			client.mutateRowTs(tableNameBuffer, row, mutations, timestamp);
		} catch(IOError ioe) {
			logger.error("JVMData "+ioe.getMessage());
		} catch (Exception e) {
			System.out.println(e.getClass().getName());
			e.printStackTrace();
		}
	}
}

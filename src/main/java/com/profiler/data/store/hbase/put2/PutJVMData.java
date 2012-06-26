package com.profiler.data.store.hbase.put2;

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
import java.util.Date;

import org.apache.hadoop.hbase.thrift2.generated.THBaseService;
import org.apache.hadoop.hbase.thrift2.generated.TIOError;
import org.apache.hadoop.hbase.thrift2.generated.TPut;
import org.apache.thrift.transport.TTransportException;

import com.profiler.dto.JVMInfoThriftDTO;
public class PutJVMData extends AbstractPutData{
	JVMInfoThriftDTO dto=null;
	public PutJVMData(String tableName,JVMInfoThriftDTO dto) {
		super(tableName);
		this.dto=dto;
	}
	@Override
	protected void writeData(ByteBuffer tableNameBuffer, THBaseService.Client client) {
		try {
			long timestamp=dto.getDataTime();
			String timestampString=timestamp+"";
			String rowName=DATE_FORMAT_YMD.format(new Date(timestamp));
			ByteBuffer row=ByteBuffer.wrap(rowName.getBytes());
			
			TPut jvmPut=new TPut();
			
			jvmPut.addToColumnValues(getTColumnValue(HBASE_JVM_COLUMN_ACTIVE_THREAD_COUNT,timestampString,dto.getActiveThreadCount()+""));
			jvmPut.addToColumnValues(getTColumnValue(HBASE_JVM_COLUMN_GC1_COUNT,timestampString,dto.getGc1Count()+""));
			jvmPut.addToColumnValues(getTColumnValue(HBASE_JVM_COLUMN_GC1_TIME,timestampString,dto.getGc1Time()+""));
			jvmPut.addToColumnValues(getTColumnValue(HBASE_JVM_COLUMN_GC2_COUNT,timestampString,dto.getGc2Count()+""));
			jvmPut.addToColumnValues(getTColumnValue(HBASE_JVM_COLUMN_GC2_TIME,timestampString,dto.getGc2Time()+""));
			jvmPut.addToColumnValues(getTColumnValue(HBASE_JVM_COLUMN_HEAP_USED,timestampString,dto.getHeapUsed()+""));
			jvmPut.addToColumnValues(getTColumnValue(HBASE_JVM_COLUMN_HEAP_COMMITTED,timestampString,dto.getHeapCommitted()+""));
			jvmPut.addToColumnValues(getTColumnValue(HBASE_JVM_COLUMN_NON_HEAP_USED,timestampString,dto.getNonHeapUsed()+""));
			jvmPut.addToColumnValues(getTColumnValue(HBASE_JVM_COLUMN_NON_HEAP_COMMITTED,timestampString,dto.getNonHeapCommitted()+""));
			jvmPut.addToColumnValues(getTColumnValue(HBASE_JVM_COLUMN_PROCESS_CPU_TIME,timestampString,dto.getProcessCPUTime()+""));
			
			jvmPut.setRow(row);
//			System.out.println(jvmPut);
			client.put(tableNameBuffer, jvmPut);
		} catch (TTransportException tte) {
			logger.error(tte);
			System.err.println(tte.getMessage());
		} catch (TIOError tioe) {
			logger.error(tioe);
			System.err.println(tioe.getMessage());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
}

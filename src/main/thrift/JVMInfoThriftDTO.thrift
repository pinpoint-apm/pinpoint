namespace java com.profiler.dto

struct JVMInfoThriftDTO {
	1: i32		agentHashCode,
	2: i64 		dataTime,
	3: i32		activeThreadCount,
	4: optional i64		gc1Count,
	5: optional i64		gc1Time,
	6: optional i64		gc2Count,
	7: optional i64		gc2Time,
	8: i64		heapUsed,
	9: i64		heapCommitted,
	10: i64		nonHeapUsed,
	11:	i64		nonHeapCommitted,
	12: optional double		processCPUTime
	
}

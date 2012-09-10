namespace java com.profiler.common.dto.thrift

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

struct RequestDataThriftDTO {
	1: i32		dataType,
	2: i64 		dataTime,
	3: optional i32		dataHashCode,
	4: optional string dataString,
	5: optional string extraData1,
	6: optional string extraData2,
	7: optional string extraData3,
	8: optional i32 extraInt1,
	9: optional i32 extraInt2,
	10: optional i32 extraInt3,
	11: optional i64 extraLong1,
	12: optional i64 extraLong2,
	13: optional i64 extraLong3
}

struct RequestDataListThriftDTO {
	1: i32		hostHashCode,
	2: i32		requestHashCode,
	3: list<RequestDataThriftDTO> requestDataList
}

struct RequestThriftDTO {
	1: i32		hostHashCode,
	2: i32		requestHashCode,
	3: i32		dataType,
	4: i64 		dataTime,
	5: i64		threadCPUTime,
	6: i64		threadUserTime,
	7: optional string 	requestID,
	8: optional string 	requestURL,
	9: optional string 	clientIP,
	10: optional string extraData1,
	11: optional string extraData2,
	12: optional string extraData3,
	13: optional i32 extraInt1,
	14: optional i32 extraInt2,
	15: optional i32 extraInt3,
	16: optional i64 extraLong1,
	17: optional i64 extraLong2,
	18: optional i64 extraLong3
}
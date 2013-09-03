namespace java com.nhn.pinpoint.thrift.dto

struct AgentInfo {
	1: string	hostname
	2: string	ip
	3: string	ports
	4: string	agentId
	5: string	applicationName
	6: i16	serviceType
	7: bool	isAlive
	8: i64	timestamp
	9: i16  identifier
}

struct StatWithCmsCollector {
	1: string	agentId
	2: i64		timestamp
	3: i64		jvmMemoryTotalInit
	4: i64		jvmMemoryTotalUsed
	5: i64		jvmMemoryTotalCommitted
	6: i64		jvmMemoryTotalMax
	7: i64		jvmMemoryHeapInit
	8: i64		jvmMemoryHeapUsed
	9: i64		jvmMemoryHeapCommitted
	10: i64		jvmMemoryHeapMax
	11: i64		jvmMemoryNonHeapInit
	12: i64		jvmMemoryNonHeapUsed
	13: i64		jvmMemoryNonHeapCommitted
	14: i64		jvmMemoryNonHeapMax
	15: i64		jvmGcParNewCount
	16: i64		jvmGcParNewTime
	17: i64		jvmGcCmsCount
	18: i64		jvmGcTime
	200: optional string metadata
}

struct StatWithG1Collector {
	1: string	agentId
	2: i64		timestamp
}

struct StatWithParallelCollector {
	1: string	agentId
	2: i64		timestamp
}

union AgentStat {
	1: StatWithCmsCollector			cms
	2: StatWithG1Collector			g1
	3: StatWithParallelCollector	parallel
}

struct JVMInfoThriftDTO {
	1: string	agentId,
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
	1: string	agentId,
	2: i32		requestHashCode,
	3: list<RequestDataThriftDTO> requestDataList
}

struct RequestThriftDTO {
	1: string	agentId,
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

struct Result {
  1: bool success
  2: optional string message
}


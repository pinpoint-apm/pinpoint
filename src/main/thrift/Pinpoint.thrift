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

struct StatWithSerialCollector {
	1: string	agentId
	2: i64		timestamp
	3: i64		jvmMemoryTotalUsed
	4: i64		jvmMemoryTotalMax
	5: i64		jvmMemoryHeapUsed
	6: i64		jvmMemoryHeapMax
	7: i64		jvmMemoryNonHeapUsed
	8: i64		jvmMemoryNonHeapMax
	9: i64		jvmMemoryPoolsCodeCacheUsage
	10: i64		jvmMemoryPoolsEdenSpaceUsage
	11: i64		jvmMemoryPoolsSurvivorSpaceUsage
	12: i64		jvmMemoryPoolsPermGenUsage
	13: i64		jvmMemoryPoolsTenuredGenUsage
	14: i64		jvmGcCopyCount
	15: i64		jvmGcCopyTime
	16: i64		jvmGcMarkSweepCompactCount
	17: i64		jvmGcMarkSweepCompactTime
	200: optional string metadata
}

struct StatWithParallelCollector {
	1: string	agentId
	2: i64		timestamp
	3: i64		jvmMemoryTotalUsed
	4: i64		jvmMemoryTotalMax
	5: i64		jvmMemoryHeapUsed
	6: i64		jvmMemoryHeapMax
	7: i64		jvmMemoryNonHeapUsed
	8: i64		jvmMemoryNonHeapMax
	9: i64		jvmMemoryPoolsCodeCacheUsage
	10: i64		jvmMemoryPoolsPSEdenSpaceUsage
	11: i64		jvmMemoryPoolsPSSurvivorSpaceUsage
	12: i64		jvmMemoryPoolsPSPermGenUsage
	13: i64		jvmMemoryPoolsPSOldGenUsage
	14: i64		jvmGcPSMarkSweepCount
	15: i64		jvmGcPSMarkSweepTime
	16: i64		jvmGcPSScavengeCount
	17: i64		jvmGcPSScavengeTime
	200: optional string metadata
}

struct StatWithCmsCollector {
	1: string	agentId
	2: i64		timestamp
	3: i64		jvmMemoryTotalUsed
	4: i64		jvmMemoryTotalMax
	5: i64		jvmMemoryHeapUsed
	6: i64		jvmMemoryHeapMax
	7: i64		jvmMemoryNonHeapUsed
	8: i64		jvmMemoryNonHeapMax
	9: i64		jvmMemoryPoolsCodeCacheUsage
	10: i64		jvmMemoryPoolsParEdenSpaceUsage
	11: i64		jvmMemoryPoolsParSurvivorSpaceUsage
	12: i64		jvmMemoryPoolsCMSOldGenUsage
	13: i64		jvmMemoryPoolsCMSPermGenUsage
	14: i64		jvmGcParNewCount
	15: i64		jvmGcParNewTime
	16: i64		jvmGcCmsCount
	17: i64		jvmGcCmsTime
	200: optional string metadata
}

struct StatWithG1Collector {
	1: string	agentId
	2: i64		timestamp
	3: i64		jvmMemoryTotalUsed
	4: i64		jvmMemoryTotalMax
	5: i64		jvmMemoryHeapUsed
	6: i64		jvmMemoryHeapMax
	7: i64		jvmMemoryNonHeapUsed
	8: i64		jvmMemoryNonHeapMax
	9: i64		jvmMemoryPoolsCodeCacheUsage
	10: i64		jvmMemoryPoolsG1EdenSpaceUsage
	11: i64		jvmMemoryPoolsG1OldGenUsage
	12: i64		jvmMemoryPoolsG1PermGenUsage
	13: i64		jvmMemoryPoolsG1SurvivorSpaceUsage
	14: i64		jvmGcG1OldGenerationCount
	15: i64		jvmGcG1OldGenerationTime
	16: i64		jvmGcG1YoungGenerationCount
	17: i64		jvmGcG1YoungGenerationTime
	200: optional string metadata
}

union AgentStat {
	1: StatWithSerialCollector		serial
	2: StatWithParallelCollector	parallel
	3: StatWithCmsCollector			cms
	4: StatWithG1Collector			g1
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


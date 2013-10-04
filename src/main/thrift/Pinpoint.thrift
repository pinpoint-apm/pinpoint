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
	9: i32  pid
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


struct Result {
  1: bool success
  2: optional string message
}


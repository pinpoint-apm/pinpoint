namespace java com.nhn.pinpoint.thrift.dto

struct TServiceInfo {
    1: optional string          serviceName
    2: optional list<string>    serviceLibs
}

struct TServerMetaData {
    1: optional string              serverInfo
    2: optional list<string>        vmArgs
    10: optional list<TServiceInfo>  serviceInfos
}

struct TAgentInfo {
	1: string	hostname
	2: string	ip
	3: string	ports
	4: string	agentId
	5: string	applicationName
	6: i16	    serviceType
	7: i32      pid
	8: string   version;

	10: i64	    startTimestamp

	11: optional i64     endTimestamp
	12: optional i32     endStatus
	
	20: optional TServerMetaData   serverMetaData
}

enum TJvmGcType {
    UNKNOWN,
    SERIAL,
    PARALLEL,
    CMS,
    G1    
}

struct TJvmGc {
    1: TJvmGcType   type = TJvmGcType.UNKNOWN
    2: i64          jvmMemoryHeapUsed
    3: i64          jvmMemoryHeapMax
    4: i64          jvmMemoryNonHeapUsed
    5: i64          jvmMemoryNonHeapMax
    6: i64          jvmGcOldCount
    7: i64          jvmGcOldTime
}

struct TCpuLoad {
    1: optional double       jvmCpuLoad
    2: optional double       systemCpuLoad
}

struct TAgentStat {
    1: optional string      agentId
    2: optional i64         startTimestamp
    3: optional i64         timestamp
    10: optional TJvmGc     gc
    20: optional TCpuLoad   cpuLoad
    200: optional string    metadata    
}

struct TAgentStatBatch {
    1: string                   agentId
    2: i64                      startTimestamp
    10: list<TAgentStat>        agentStats
}

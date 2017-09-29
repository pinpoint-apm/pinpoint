include "Command.thrift"

namespace java com.navercorp.pinpoint.thrift.dto

enum TJvmGcType {
    UNKNOWN,
    SERIAL,
    PARALLEL,
    CMS,
    G1
}

struct TServiceInfo {
    1: optional string          serviceName
    2: optional list<string>    serviceLibs
}

struct TServerMetaData {
    1: optional string              serverInfo
    2: optional list<string>        vmArgs
    10: optional list<TServiceInfo>  serviceInfos
}

struct TJvmInfo {
    1:          i16         version = 0
    2: optional string      vmVersion
    3: optional TJvmGcType  gcType = TJvmGcType.UNKNOWN
}

struct TAgentInfo {
    1: string	hostname
    2: string	ip
    3: string	ports
    4: string	agentId
    5: string	applicationName
    6: i16	    serviceType
    7: i32      pid
    8: string   agentVersion;
    9: string   vmVersion;

    10: i64	    startTimestamp

    11: optional i64     endTimestamp
    12: optional i32     endStatus

    20: optional TServerMetaData   serverMetaData

    30: optional TJvmInfo   jvmInfo
}

struct TJvmGc {
    1: TJvmGcType   type = TJvmGcType.UNKNOWN
    2: i64          jvmMemoryHeapUsed
    3: i64          jvmMemoryHeapMax
    4: i64          jvmMemoryNonHeapUsed
    5: i64          jvmMemoryNonHeapMax
    6: i64          jvmGcOldCount
    7: i64          jvmGcOldTime
    8: optional TJvmGcDetailed    jvmGcDetailed
}

struct TJvmGcDetailed {
    1: optional i64 jvmGcNewCount
    2: optional i64 jvmGcNewTime
    3: optional double jvmPoolCodeCacheUsed
    4: optional double jvmPoolNewGenUsed
    5: optional double jvmPoolOldGenUsed
    6: optional double jvmPoolSurvivorSpaceUsed
    7: optional double jvmPoolPermGenUsed
    8: optional double jvmPoolMetaspaceUsed
}

struct TCpuLoad {
    1: optional double       jvmCpuLoad
    2: optional double       systemCpuLoad
}

struct TTransaction {
    2: optional i64     sampledNewCount
    3: optional i64     sampledContinuationCount
    4: optional i64     unsampledNewCount
    5: optional i64     unsampledContinuationCount
}

struct TActiveTraceHistogram {
    1:          i16         version = 0
    2: optional i32         histogramSchemaType
    3: optional list<i32>   activeTraceCount
}

struct TActiveTrace {
    1: optional TActiveTraceHistogram   histogram
}

struct TResponseTime {
    1: optional i64         avg = 0
}

struct TDeadlock {
    1: optional i32                         deadlockedThreadCount;
    2: optional list<Command.TThreadDump>   deadlockedThreadList;
}

struct TAgentStat {
    1: optional string      agentId
    2: optional i64         startTimestamp
    3: optional i64         timestamp
    4: optional i64         collectInterval
    10: optional TJvmGc     gc
    20: optional TCpuLoad   cpuLoad
    30: optional TTransaction   transaction
    40: optional TActiveTrace   activeTrace
    50: optional TDataSourceList dataSourceList
    60: optional TResponseTime responseTime
    70: optional TDeadlock deadlock
    200: optional string    metadata
}

struct TAgentStatBatch {
    1: string                   agentId
    2: i64                      startTimestamp
    10: list<TAgentStat>        agentStats
}

struct TDataSource {
    1: i32                      id
    2: optional i16             serviceTypeCode
    3: optional string          databaseName
    4: optional string          url
    5: optional i32             activeConnectionSize = 0
    6: optional i32             maxConnectionSize
}

struct TDataSourceList {
    1: list<TDataSource> dataSourceList
}

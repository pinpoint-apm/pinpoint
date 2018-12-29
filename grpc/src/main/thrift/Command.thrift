namespace java com.navercorp.pinpoint.thrift.dto.command

enum TThreadDumpType {
    TARGET,
    PENDING
}
struct TCommandThreadDump {
    1: TThreadDumpType   type = TThreadDumpType.TARGET
    2: optional string	name
    3: optional i64 pendingTimeMillis
}

enum TThreadState {
    NEW,
    RUNNABLE,
    BLOCKED,
    WAITING,
    TIMED_WAITING,
    TERMINATED,
    UNKNOWN
}

struct TMonitorInfo {
    1: i32 		stackDepth
    2: string 	stackFrame
}

struct TThreadDump {
    1: string   threadName
    2: i64      threadId
    3: i64      blockedTime
    4: i64      blockedCount
    5: i64      waitedTime
    6: i64      waitedCount
    7: string   lockName
    8: i64      lockOwnerId
    9: string   lockOwnerName
    10: bool    inNative
    11: bool    suspended
    12: TThreadState    threadState
    13: list<string>    stackTrace
    14: list<TMonitorInfo> lockedMonitors
    15: list<string>    lockedSynchronizers
}

struct TThreadLightDump {
    1: string     threadName
    2: i64        threadId
    3: optional TThreadState    threadState
}

struct TCommandThreadDumpResponse {
    1: list<TThreadDump> threadDumps
}


struct TCmdActiveThreadCount {
}

struct TCmdActiveThreadCountRes {
    1: i32 	histogramSchemaType
    2: list<i32> activeThreadCount
    3: optional i64 timeStamp
}


struct TActiveThreadDump {
    1: i64 startTime
    2: i64 localTraceId
    3: TThreadDump threadDump
    4: bool sampled = false
    5: optional string transactionId
    6: optional string entryPoint
}

struct TActiveThreadLightDump {
    1: i64 startTime
    2: i64 localTraceId
    3: TThreadLightDump threadDump
    4: bool sampled = false
    5: optional string transactionId
    6: optional string entryPoint
}

struct TCmdActiveThreadDump {
    1: optional i32 limit
    2: optional list<string> threadNameList
    3: optional list<i64> localTraceIdList
}

struct TCmdActiveThreadLightDump {
    1: optional i32 limit
    2: optional list<string> threadNameList
    3: optional list<i64> localTraceIdList
}

struct TCmdActiveThreadDumpRes {
    1: list<TActiveThreadDump> threadDumps
    2: optional string type
    3: optional string subType
    4: optional string version
}

struct TCmdActiveThreadLightDumpRes {
    1: list<TActiveThreadLightDump> threadDumps
    2: optional string type
    3: optional string subType
    4: optional string version
}

struct TCommandEcho {
    1: string	message
}

enum TRouteResult {
    OK = 0,

    BAD_REQUEST = 200,
    EMPTY_REQUEST = 201,
    NOT_SUPPORTED_REQUEST = 202,

    BAD_RESPONSE = 210,
    EMPTY_RESPONSE = 211,
    NOT_SUPPORTED_RESPONSE = 212,

    TIMEOUT = 220,

    NOT_FOUND = 230,

    NOT_ACCEPTABLE = 240,
    NOT_SUPPORTED_SERVICE = 241,

    UNKNOWN = -1
}

struct TCommandTransfer {
    1: string applicationName
    2: string agentId
    3: optional i64 startTime
    4: binary payload
}

struct TCommandTransferResponse {
    1: TRouteResult routeResult
    2: binary payload
    3: optional string message
}
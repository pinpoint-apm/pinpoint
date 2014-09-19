namespace java com.nhn.pinpoint.thrift.dto.command

enum TThreadDumpType {
    TARGET,
    PENDING
}
struct TCommandThreadDump {
    1: TThreadDumpType   type = TThreadDumpType.TARGET
	2: optional string	name
	3: optional i64 pendingTimeMillis
}
struct TCommandEcho {
	1: string	message
}
struct TCommandTransfer {
    1: string	applicationName
    2: string 	agentId
    3: optional i64		startTime
    4: binary 	payload
}

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

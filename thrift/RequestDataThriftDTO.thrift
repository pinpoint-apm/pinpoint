namespace java com.profiler.dto
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

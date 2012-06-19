namespace java com.profiler.dto
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


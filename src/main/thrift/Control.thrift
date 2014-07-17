namespace java com.nhn.pinpoint.thrift.dto.control

struct THello {
	1: string	hostname
	2: string	ip
	3: string	ports
	4: string	agentId
	5: string	applicationName
	6: i16	    serviceType
	7: i32      pid
	8: string   version;
	9: i64	    startTimestamp

}

struct TWelcome {
	1: i16		code
}

struct TBye {
	1: i16		code
	2: i64     	endTimestamp
}

struct TLeave {
	1: i16		code
}


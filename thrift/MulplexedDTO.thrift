include "Header.thrift"
include "JVMInfoThriftDTO.thrift"
include "RequestDataThriftDTO.thrift"
include "RequestThriftDTO.thrift"

namespace java com.profiler.dto

const i16 TYPE_JVM_INFO_DATA = 10;
struct JVMInfoData {
	1: Header.Header header,
	2: JVMInfoThriftDTO.JVMInfoThriftDTO jvmInfoThriftDTO
}

const i16 TYPE_REQUEST_DATA = 20;

struct RequestDataList {
	1: Header.Header header,
	2: RequestDataThriftDTO.RequestDataListThriftDTO requestDataListThriftDTO
}

const i16 TYPE_REQUEST = 30;
struct Request {
	1: Header.Header header,
	2: RequestThriftDTO.RequestThriftDTO request
}







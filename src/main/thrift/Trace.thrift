namespace java com.profiler.common.dto.thrift

const string CLIENT_SEND = "CS"
const string CLIENT_RECV = "CR"
const string SERVER_SEND = "SS"
const string SERVER_RECV = "SR"

struct Endpoint {
  1: string ip,
  2: i16 port,
}

struct Annotation {
  1: i64 timestamp,
  2: string value,
  3: optional i64 duration,
}

struct BinaryAnnotation {
  1: i64 timestamp, 
  2: string key,
  3: binary value,
  4: string valueType,
}

struct Span {
  1: string agentID
  2: i64 timestamp,
  3: i64 mostTraceID
  4: i64 leastTraceID
  5: string name,
  6: string serviceName
  7: i64 spanID,
  8: optional i64 parentSpanId,
  9: list<Annotation> annotations,
  10: list<BinaryAnnotation> binaryAnnotations
  11: optional i32 flag = 0
}
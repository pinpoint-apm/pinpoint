namespace java com.profiler.context.gen

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
  1: i64 timestamp,
  2: i64 mostTraceID
  3: i64 leastTraceID
  4: string name,
  5: i64 spanID,
  6: optional i64 parentSpanId,
  7: list<Annotation> annotations,
  8: list<BinaryAnnotation> binaryAnnotations
  9: optional i32 flag = 0
}
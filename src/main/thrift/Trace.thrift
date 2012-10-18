namespace java com.profiler.common.dto.thrift

struct Annotation {
  1: i64 timestamp,
  2: optional i64 duration,
  3: string key,
  4: i32 valueTypeCode,
  5: optional binary value,
}

struct Span {
  1: string agentId
  2: i64 timestamp,
  3: i64 mostTraceId
  4: i64 leastTraceId
  5: string name,
  6: string serviceName
  7: i64 spanId,
  8: optional i64 parentSpanId,
  9: list<Annotation> annotations,
  10: optional i32 flag = 0
  11: string endPoint
  12: bool terminal
}
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
  2: i64 mostTraceId
  3: i64 leastTraceId
  4: i64 startTime,
  5: i64 endTime,
  6: string name,
  7: string serviceName
  8: i64 spanId,
  9: optional i64 parentSpanId,
  10: list<Annotation> annotations,
  11: optional i32 flag = 0
  12: string endPoint
  13: bool terminal
}
namespace java com.profiler.common.dto.thrift

struct Annotation {
  1: string key,
  2: i32 valueTypeCode,
  3: optional binary value,
  4: i64 timestamp,
}

struct Span {
  1: string agentId
  
  2: i64 mostTraceId
  3: i64 leastTraceId
  
  4: i64 spanId
  5: i64 parentSpanId
  
  6: i64 startTime
  7: i32 elapsed
  
  8: string rpc
  9: string serviceName
  10: i16 serviceType
  11: string endPoint
  12: bool terminal
  
  13: list<Annotation> annotations
  14: optional i16 flag = 0
}
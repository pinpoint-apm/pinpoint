namespace java com.profiler.common.dto.thrift

struct Annotation {
  1: string key,
  2: i32 valueTypeCode,
  3: optional binary value,
  4: i64 timestamp,
}

struct ApiAnnotation {
  1: string className,
  2: string methodName,
  3: list<string> parameterType,
  4: optional list<string> parameterName,
  5: optional list<string> parameterValue
  6: optional i32 line,
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
  
  12: list<Annotation> annotations
  13: optional i16 flag = 0
  
  14: list<TerminalSpan> spans
}

struct TerminalSpan {
  1: i64 startTime
  2: i32 elapsed
  
  3: string rpc
  4: string serviceName
  5: i16 serviceType
  6: string endPoint
  
  7: list<Annotation> annotations
}
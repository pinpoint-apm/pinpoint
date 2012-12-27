namespace java com.profiler.common.dto.thrift

struct Annotation {
  1: string key,
  2: i32 valueTypeCode,
  3: optional binary value,
  4: optional i64 timestamp,
}

struct ApiAnnotation {
  1: optional i32 apiId,
  2: optional string className,
  3: optional string methodName,
  4: optional list<string> parameterType,
  5: optional list<string> parameterName,
  6: optional list<string> parameterValue,
  7: optional i32 line,
}

struct SubSpan {
  1: optional string agentId

  2: optional i64 mostTraceId
  3: optional i64 leastTraceId

  4: optional i64 spanId
  5: i16 sequence

  6: i32 startElapsed
  7: i32 endElapsed

  8: string rpc
  9: string serviceName
  10: i16 serviceType
  11: string endPoint

  12: bool err

  13: list<Annotation> annotations
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

  14: bool err

  15: optional list<SubSpan> subSpanList
}



struct SubSpanList {
  1: string agentId

  2: i64 mostTraceId
  3: i64 leastTraceId

  4: i64 spanId

  6: list<SubSpan> subSpanList
}


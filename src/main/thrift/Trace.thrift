namespace java com.profiler.common.dto.thrift

struct Annotation {
  1: string key,
  2: i32 valueTypeCode,
  3: optional binary value,
  4: optional i64 timestamp,
}

struct SubSpan {
  1: optional string agentId

  2: optional i64 mostTraceId
  3: optional i64 leastTraceId

  4: optional i32 spanId
  5: i16 sequence

  6: i32 startElapsed
  7: i32 endElapsed

  8: optional string rpc
  9: optional string serviceName
  10: optional i16 serviceType
  11: optional string endPoint

  12: bool err

  13: list<Annotation> annotations
  
  14: optional i32 depth
  15: optional i32 nextSpanId
}

struct Span {
  1: string agentId
  
  2: i64 mostTraceId
  3: i64 leastTraceId
  
  4: i32 spanId
  5: i32 parentSpanId
  
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

  4: i32 spanId

  5: list<SubSpan> subSpanList
}

struct SqlMetaData {
    1: string agentId
    2: i64 startTime;
    3: i32 hashCode
    4: string sql;
}


struct ApiMetaData {
  1: string agentId
  2: i64 startTime;
  3: i32 apiId,
  4: string apiInfo,
  5: optional i32 line,
}
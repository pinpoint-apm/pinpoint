namespace java com.profiler.common.dto.thrift

struct Annotation {
  1: i32 key,

  2: optional string stringValue
  3: optional bool boolValue;
  4: optional i32 intValue
  5: optional i64 longValue;
  6: optional i16 shortValue
  7: optional double doubleValue;
  8: optional binary binaryValue;
  9: optional byte byteValue;
}

struct SubSpan {
  1: optional string agentId
  2: optional i16 agentIdentifier

  3: optional i64 mostTraceId
  4: optional i64 leastTraceId

  5: optional i32 spanId
  6: i16 sequence

  7: i32 startElapsed
  8: i32 endElapsed

  9: optional string rpc
  10: optional string serviceName
  11: optional i16 serviceType
  12: optional string endPoint

  14: list<Annotation> annotations
  
  15: optional i32 depth
  16: optional i32 nextSpanId
}

struct Span {
  1: string agentId
  2: i16 agentIdentifier
  
  3: i64 mostTraceId
  4: i64 leastTraceId
  
  5: i32 spanId
  6: optional i32 parentSpanId = -1
  
  7: i64 startTime
  8: i32 elapsed
  
  9: string rpc
  10: string serviceName
  11: i16 serviceType
  12: string endPoint
  13: optional string remoteAddr 
  
  14: list<Annotation> annotations
  15: optional i16 flag = 0

  16: optional i32 err

  17: optional list<SubSpan> subSpanList
}

struct SubSpanList {
  1: string agentId
  2: i16 agentIdentifier

  3: i64 mostTraceId
  4: i64 leastTraceId

  5: i32 spanId

  6: list<SubSpan> subSpanList
}

struct SqlMetaData {
    1: string agentId
    2: i16 agentIdentifier
    3: i64 startTime;
    4: i32 hashCode
    5: string sql;
}


struct ApiMetaData {
  1: string agentId
  2: i16 agentIdentifier
  3: i64 startTime;
  4: i32 apiId,
  5: string apiInfo,
  6: optional i32 line,
}
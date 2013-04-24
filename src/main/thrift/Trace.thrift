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

struct SpanEvent {
  1: optional string agentId
  2: optional string applicationId
  17: optional i16 parentServiceType
  18: optional string parentEndPoint
  3: optional i16 agentIdentifier

  4: optional i64 mostTraceId
  5: optional i64 leastTraceId

  6: optional i32 spanId
  7: i16 sequence

  8: i32 startElapsed
  9: i32 endElapsed

  10: optional string rpc
  12: i16 serviceType
  13: optional string endPoint

  14: list<Annotation> annotations
  
  15: optional i32 depth
  16: optional i32 nextSpanId

  20: optional string destinationId
  // address주소가 1개일 경우
  21: optional list<string> destinationAddress;
  // address주소가 2개이상일 경우
  //15: optional list<string> destinationAddressList;
}

struct Span {
  1: string agentId
  2: string applicationId
  3: i16 agentIdentifier
  
  4: i64 mostTraceId
  5: i64 leastTraceId
  
  6: i32 spanId
  7: optional i32 parentSpanId = -1
  
  8: i64 startTime
  9: i32 elapsed
  
  10: optional string rpc
//  11: optional string serviceName
  12: i16 serviceType
  13: optional string endPoint
  14: optional string remoteAddr
  
  15: list<Annotation> annotations
  16: optional i16 flag = 0

  17: optional i32 err

  18: optional list<SpanEvent> spanEventList
  
  19: optional string parentApplicationName
  20: optional i16 parentApplicationType
  21: optional string acceptorHost
}

struct SpanChunk {
  1: string agentId
  2: string applicationId
  3: i16 serviceType
  4: i16 agentIdentifier

  5: i64 mostTraceId
  6: i64 leastTraceId

  7: i32 spanId
  
  8: optional string endPoint

  9: list<SpanEvent> spanEventList
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
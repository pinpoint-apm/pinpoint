namespace java com.nhn.pinpoint.thrift.dto

struct TSqlValue {
    1: i32 id;
    2: optional string bindValue;
}

struct TExceptionValue {
    1: i32 id;
    2: optional string exceptionMessage;
}

union TAnnotationValue {
  1: string stringValue
  2: bool boolValue;
  3: i32 intValue;
  4: i64 longValue;
  5: i16 shortValue
  6: double doubleValue;
  7: binary binaryValue;
  8: byte byteValue;
  9: TSqlValue sqlValue;
}

struct TAnnotation {
  1: i32 key,
  2: optional TAnnotationValue value
}



struct TAgentKey {
    1: string agentId;
    2: string applicationName;
    3: i64 agentStartTime;
}

struct TSpanEvent {
  // spanEvent의 agentKey는 매우 특별한 경우에만 생성되므로 필드사이즈를 줄이기 위해 별도 객체로 분리.
  1: optional TAgentKey agentKey;

  17: optional i16 parentServiceType
  18: optional string parentEndPoint

  // null일 경우 agentId와 동일한 값이다.
  4: optional string traceAgentId
  5: optional i64 traceAgentStartTime;
  6: optional i64 traceTransactionSequence;

  7: optional i32 spanId
  8: i16 sequence

  9: i32 startElapsed
  10: i32 endElapsed

  11: optional string rpc
  12: i16 serviceType
  13: optional string endPoint

  14: optional list<TAnnotation> annotations

  15: optional i32 depth = -1
  16: optional i32 nextSpanId = -1

  20: optional string destinationId

  25: optional i32 apiId;
  26: optional i32 exceptionId;
}

struct TSpan {

  1: string agentId
  2: string applicationName
  3: i64 agentStartTime

  // null일 경우 agentId와 동일한 값이다.
  4: optional string traceAgentId
  5: i64 traceAgentStartTime;
  6: i64 traceTransactionSequence;

  7: i32 spanId
  8: optional i32 parentSpanId = -1

  // span 이벤트의 시작시간.
  9: i64 startTime
  10: i32 elapsed

  11: optional string rpc

  12: i16 serviceType
  13: optional string endPoint
  14: optional string remoteAddr

  15: optional list<TAnnotation> annotations
  16: optional i16 flag = 0

  17: optional i32 err

  18: optional list<TSpanEvent> spanEventList

  19: optional string parentApplicationName
  20: optional i16 parentApplicationType
  21: optional string acceptorHost

  25: optional i32 apiId;
  26: optional i32 exceptionId;
}

struct TSpanChunk {
  1: string agentId
  2: string applicationName
  3: i64 agentStartTime

  4: i16 serviceType

  // null일 경우 agentId와 동일한 값이다.
  5: optional string traceAgentId
  6: i64 traceAgentStartTime;
  7: i64 traceTransactionSequence;

  8: i32 spanId

  9: optional string endPoint

  10: list<TSpanEvent> spanEventList
}

struct TSqlMetaData {

    1: string agentId
    2: i64 agentStartTime

    4: i32 hashCode
    5: string sql;
}


struct TApiMetaData {
  1: string agentId
  2: i64 agentStartTime

  4: i32 apiId,
  5: string apiInfo,
  6: optional i32 line,
}

struct TResult {
  1: bool success
  2: optional string message
}
namespace java com.navercorp.pinpoint.thrift.dto
// 1.6.x- : version = 0;
// 1.7.x+ : version = 1;
const i8 TRACE_V1 = 0;
const i8 TRACE_V2 = 1;

struct TIntStringValue {
     1: i32 intValue;
     2: optional string stringValue;
}

struct TIntStringStringValue {
    1: i32 intValue;
    2: optional string stringValue1;
    3: optional string stringValue2;
}

struct TLongIntIntByteByteStringValue {
    1: i64 longValue;
    2: i32 intValue1;
    3: optional i32 intValue2;
    4: optional i8 byteValue1;
    5: optional i8 byteValue2;
    6: optional string stringValue;
}

struct TIntBooleanIntBooleanValue {
    1: i32 intValue1;
    2: bool boolValue1;
    3: i32 intValue2;
    4: bool boolValue2;
}

union TAnnotationValue {
    1: string stringValue
    2: bool boolValue;
    3: i32 intValue;
    4: i64 longValue;
    5: i16 shortValue
    6: double doubleValue;
    7: binary binaryValue;
    8: i8 byteValue;
    9: TIntStringValue intStringValue;
    10: TIntStringStringValue intStringStringValue;
    11: TLongIntIntByteByteStringValue longIntIntByteByteStringValue;
    12: TIntBooleanIntBooleanValue intBooleanIntBooleanValue;
}

struct TAnnotation {
    1: i32 key,
    2: optional TAnnotationValue value
}

struct TSpanEvent {

    7: optional i64 spanId
    8: i16 sequence

    // 1.6.x- : delta of the span startTime
    // 1.7.0+: delta of startTime of previous SpanEvent
    //         If SpanEvent is the first SpanEvent, startElapsed is span startTime
    9: i32 startElapsed = 0;

    10: optional i32 endElapsed = 0

    11: optional string rpc
    12: i16 serviceType
    13: optional string endPoint

    14: optional list<TAnnotation> annotations

    15: optional i32 depth = -1
    16: optional i64 nextSpanId = -1

    20: optional string destinationId

    25: optional i32 apiId;
    26: optional TIntStringValue exceptionInfo;

    30: optional i32 asyncId;
    31: optional i32 nextAsyncId;
    32: optional i16 asyncSequence;
}

struct TSpan {

    1: string agentId
    2: string applicationName
    3: i64 agentStartTime

    // identical to agentId if null
    //4: optional string traceAgentId
    //5: i64 traceAgentStartTime;
    //6: i64 traceTransactionSequence;
    4: binary  transactionId;

    7: i64 spanId
    8: optional i64 parentSpanId = -1

    // span event's startTimestamp
    9: i64 startTime
    10: optional i32 elapsed = 0

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
    26: optional TIntStringValue exceptionInfo;
  
    30: optional i16 applicationServiceType;
    31: optional i8 loggingTransactionInfo;

    32: optional i8 version = TRACE_V2;
}

struct TSpanChunk {
    1: string agentId
    2: string applicationName
    3: i64 agentStartTime

    // @deprecate (1.7.0)
    4: i16 serviceType ( deprecated )

    // identical to agentId if null
    //5: optional string traceAgentId
    //6: i64 traceAgentStartTime;
    //7: i64 traceTransactionSequence;
    5: binary  transactionId;

    8: i64 spanId

    9: optional string endPoint

    10: list<TSpanEvent> spanEventList
  
    11: optional i16 applicationServiceType

    // @since 1.7.0 time for data compression
    12: optional i64 keyTime;

    13: optional i8 version = TRACE_V2;
}


struct TStringMetaData {

    1: string agentId
    2: i64 agentStartTime

    4: i32 stringId
    5: string stringValue;
}

struct TSqlMetaData {

    1: string agentId
    2: i64 agentStartTime

    4: i32 sqlId
    5: string sql;
}


struct TApiMetaData {
    1: string agentId
    2: i64 agentStartTime

    4: i32 apiId,
    5: string apiInfo,
    6: optional i32 line,
  
    10: optional i32 type;
}

struct TResult {
    1: bool success
    2: optional string message
}
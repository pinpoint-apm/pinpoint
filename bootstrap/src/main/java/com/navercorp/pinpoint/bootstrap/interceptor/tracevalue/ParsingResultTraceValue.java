package com.nhn.pinpoint.bootstrap.interceptor.tracevalue;

import com.nhn.pinpoint.common.util.ParsingResult;

/**
 * @author emeroad
 */
public interface ParsingResultTraceValue extends TraceValue {
    void __setTraceParsingResult(ParsingResult parsingResult);

    ParsingResult __getTraceParsingResult();
}

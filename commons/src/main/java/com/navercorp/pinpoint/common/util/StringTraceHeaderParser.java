/*
 * Copyright 2014 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.navercorp.pinpoint.common.util;

/**
 * @author emeroad
 */
public class StringTraceHeaderParser {

    //    request.addHeader(Header.HTTP_TRACE_ID.toString(), nextId.getId().toString());
//    request.addHeader(Header.HTTP_SPAN_ID.toString(), Long.toString(nextId.getSpanId()));
//    request.addHeader(Header.HTTP_PARENT_SPAN_ID.toString(), Long.toString(nextId.getParentSpanId()));
//    request.addHeader(Header.HTTP_SAMPLED.toString(), String.valueOf(nextId.isSampled()));
//    request.addHeader(Header.HTTP_FLAGS.toString(), String.valueOf(nextId.getFlags()));
    public static final char DELIMITER_STRING = ':';
    public static final int ID_INDEX = 36;

    public String createHeader(String uuid, int spanId, int parentSpanId, int sampling, short flag) {
        StringBuilder sb = new StringBuilder(128);
        sb.append(uuid);
        sb.append(DELIMITER_STRING);
        sb.append(spanId);
        sb.append(DELIMITER_STRING);
        sb.append(parentSpanId);
        sb.append(DELIMITER_STRING);
        sb.append(sampling);
        sb.append(DELIMITER_STRING);
        sb.append(flag);
        return sb.toString();
    }

    public TraceHeader parseHeader(String traceHeader) {
        if (traceHeader == null) {
            return null;
        }

        char c = traceHeader.charAt(ID_INDEX);
        if (c != DELIMITER_STRING) {
            return null;
        }
        String id = traceHeader.substring(0, ID_INDEX);

        int spanIdStartIndex = ID_INDEX + 1;
        int spanIdEndIndex = traceHeader.indexOf(DELIMITER_STRING, spanIdStartIndex);
        String spanId = traceHeader.substring(spanIdStartIndex, spanIdEndIndex);

        int parentSpanIdStartIndex = spanIdEndIndex + 1;
        int parentSpanIdEndIndex = traceHeader.indexOf(DELIMITER_STRING, parentSpanIdStartIndex);
        String parentSpanId = traceHeader.substring(parentSpanIdStartIndex, parentSpanIdEndIndex);

        int samplingStartIndex = parentSpanIdEndIndex + 1;
        int samplingEndIndex = traceHeader.indexOf(DELIMITER_STRING, samplingStartIndex);
        if (samplingEndIndex == -1) {
            return new TraceHeader(id, spanId, parentSpanId, "", "");
        }
        String sampling = traceHeader.substring(samplingStartIndex, samplingEndIndex);


        int flagStartIndex = samplingEndIndex + 1;
        if (flagStartIndex == -1) {
            return new TraceHeader(id, spanId, parentSpanId, sampling, "");
        }
        int flagEndIndex = traceHeader.indexOf(DELIMITER_STRING, flagStartIndex);
        if (flagEndIndex == -1) {
            String flag = traceHeader.substring(flagStartIndex);
            return new TraceHeader(id, spanId, parentSpanId, sampling, flag);
        }
        String flag = traceHeader.substring(flagStartIndex, flagEndIndex);
        return  new TraceHeader(id, spanId, parentSpanId, sampling, flag);
    }


}

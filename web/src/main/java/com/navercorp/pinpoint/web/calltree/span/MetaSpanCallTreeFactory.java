/*
 * Copyright 2019 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.navercorp.pinpoint.web.calltree.span;

import com.navercorp.pinpoint.common.server.bo.AnnotationBo;
import com.navercorp.pinpoint.common.server.bo.ApiMetaDataBo;
import com.navercorp.pinpoint.common.server.bo.MethodTypeEnum;
import com.navercorp.pinpoint.common.server.bo.SpanBo;
import com.navercorp.pinpoint.common.trace.AnnotationKey;
import com.navercorp.pinpoint.common.trace.ServiceType;
import com.navercorp.pinpoint.common.util.AnnotationKeyUtils;
import com.navercorp.pinpoint.common.profiler.util.TransactionId;

import java.util.ArrayList;
import java.util.List;

/**
 * @author jaehong.kim
 */
public class MetaSpanCallTreeFactory {
    private static final long DEFAULT_TIMEOUT_MILLISEC = 60 * 1000;

    private long timeoutMillisec = DEFAULT_TIMEOUT_MILLISEC;

    public CallTree unknown(final long startTimeMillis) {
        final SpanBo rootSpan = new SpanBo();
        rootSpan.setTransactionId(new TransactionId("UNKNOWN", 0, 0));
        rootSpan.setAgentId("UNKNOWN");
        rootSpan.setApplicationId("UNKNOWN");
        rootSpan.setStartTime(startTimeMillis);
        rootSpan.setServiceType(ServiceType.UNKNOWN.getCode());

        List<AnnotationBo> annotations = new ArrayList<>();
        ApiMetaDataBo apiMetaData = new ApiMetaDataBo();
        apiMetaData.setLineNumber(-1);
        apiMetaData.setApiInfo("Unknown");
        apiMetaData.setMethodTypeEnum(MethodTypeEnum.WEB_REQUEST);

        final AnnotationBo apiMetaDataAnnotation = new AnnotationBo(AnnotationKey.API_METADATA.getCode(), apiMetaData);
        annotations.add(apiMetaDataAnnotation);

        final AnnotationBo argumentAnnotation = new AnnotationBo(AnnotationKeyUtils.getArgs(0).getCode(), "No Agent Data");
        annotations.add(argumentAnnotation);
        rootSpan.setAnnotationBoList(annotations);

        return new MetaSpanCallTree(new SpanAlign(rootSpan, true));
    }

    public SpanCallTree corrupted(final String title, final long parentSpanId, final long spanId, final long startTimeMillis) {
        final SpanBo rootSpan = new SpanBo();
        rootSpan.setParentSpanId(parentSpanId);
        rootSpan.setSpanId(spanId);
        rootSpan.setStartTime(startTimeMillis);

        rootSpan.setTransactionId(new TransactionId("CORRUPTED", 0, 0));
        rootSpan.setAgentId("CORRUPTED");
        rootSpan.setApplicationId("CORRUPTED");
        rootSpan.setServiceType(ServiceType.UNKNOWN.getCode());

        List<AnnotationBo> annotations = new ArrayList<>();

        ApiMetaDataBo apiMetaData = new ApiMetaDataBo();
        apiMetaData.setLineNumber(-1);
        apiMetaData.setApiInfo("...");
        apiMetaData.setMethodTypeEnum(MethodTypeEnum.CORRUPTED);

        final AnnotationBo apiMetaDataAnnotation = new AnnotationBo(AnnotationKey.API_METADATA.getCode(), apiMetaData);
        annotations.add(apiMetaDataAnnotation);


        int key = AnnotationKeyUtils.getArgs(0).getCode();
        String errorMessage = getErrorMessage(title, startTimeMillis);
        final AnnotationBo argumentAnnotation = new AnnotationBo(key, errorMessage);
        annotations.add(argumentAnnotation);
        rootSpan.setAnnotationBoList(annotations);
        return new MetaSpanCallTree(new SpanAlign(rootSpan, true));
    }

    private String getErrorMessage(String title, long startTimeMillis) {
        if (System.currentTimeMillis() - startTimeMillis < timeoutMillisec) {
            return "Corrupted(waiting for packet) ";
        } else {
            if (title != null) {
                return "Corrupted(lost packet - " + title + ")";
            } else {
                return "Corrupted(lost packet)";
            }
        }
    }
}
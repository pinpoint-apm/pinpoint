/*
 * Copyright 2015 NAVER Corp.
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

package com.navercorp.pinpoint.web.calltree.span;

import java.util.ArrayList;
import java.util.List;

import com.navercorp.pinpoint.common.server.bo.AnnotationBo;
import com.navercorp.pinpoint.common.server.bo.ApiMetaDataBo;
import com.navercorp.pinpoint.common.server.bo.MethodTypeEnum;
import com.navercorp.pinpoint.common.server.bo.SpanBo;
import com.navercorp.pinpoint.common.server.bo.SpanEventBo;
import com.navercorp.pinpoint.common.trace.AnnotationKey;
import com.navercorp.pinpoint.common.trace.ServiceType;
import com.navercorp.pinpoint.common.util.AnnotationKeyUtils;

/**
 * 
 * @author jaehong.kim
 *
 */
public class CorruptedSpanAlignFactory {
    // private static final long DEFAULT_TIMEOUT_MILLISEC = 10 * 60 * 1000;
    private static final long DEFAULT_TIMEOUT_MILLISEC = 60 * 1000;

    private long timeoutMillisec = DEFAULT_TIMEOUT_MILLISEC;

    public SpanAlign get(final String title, final SpanBo span, final SpanEventBo spanEvent) {
        final SpanEventBo missedEvent = new SpanEventBo();
        // TODO use invalid event information ?
        missedEvent.setStartElapsed(spanEvent.getStartElapsed());
        missedEvent.setEndElapsed(spanEvent.getEndElapsed());

        missedEvent.setServiceType(ServiceType.COLLECTOR.getCode());

        List<AnnotationBo> annotations = new ArrayList<>();

        ApiMetaDataBo apiMetaData = new ApiMetaDataBo();
        apiMetaData.setLineNumber(-1);
        apiMetaData.setApiInfo("...");
        apiMetaData.setMethodTypeEnum(MethodTypeEnum.CORRUPTED);

        final AnnotationBo apiMetaDataAnnotation = new AnnotationBo();
        apiMetaDataAnnotation.setKey(AnnotationKey.API_METADATA.getCode());
        apiMetaDataAnnotation.setValue(apiMetaData);
        annotations.add(apiMetaDataAnnotation);

        final AnnotationBo argumentAnnotation = new AnnotationBo();
        argumentAnnotation.setKey(AnnotationKeyUtils.getArgs(0).getCode());
        if (System.currentTimeMillis() - span.getStartTime() < timeoutMillisec) {
            argumentAnnotation.setValue("Corrupted(waiting for packet) ");
        } else {
            if (title != null) {
                argumentAnnotation.setValue("Corrupted(" + title + ")");
            } else {
                argumentAnnotation.setValue("Corrupted");
            }
        }
        annotations.add(argumentAnnotation);

        missedEvent.setAnnotationBoList(annotations);

        return new SpanAlign(span, missedEvent);
    }
}
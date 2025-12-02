/*
 * Copyright 2025 NAVER Corp.
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

package com.navercorp.pinpoint.web.trace.callstacks;

import com.navercorp.pinpoint.common.server.bo.AnnotationBo;
import com.navercorp.pinpoint.common.server.bo.ExceptionInfo;
import com.navercorp.pinpoint.common.trace.AnnotationKey;
import com.navercorp.pinpoint.common.trace.ServiceType;
import com.navercorp.pinpoint.web.trace.span.Align;

import java.util.List;
import java.util.Objects;

/**
 * @author jaehong.kim
 */
public class ExceptionRecord extends BaseRecord {

    public ExceptionRecord(
            final int tab, final int id, final int parentId, final Align align,
            ServiceType applicationServiceType
    ) {
        this.begin = align.getStartTime();
        this.elapsed = align.getElapsed();
        this.tab = tab;
        this.id = id;
        this.parentId = parentId;
        this.title = toSimpleExceptionName(align.getExceptionClass());
        this.arguments = buildArgument(align);
        this.isAuthorized = true;
        this.hasException = !align.isSpan();
        this.agentId = align.getAgentId();
        this.applicationName = align.getApplicationName();
        this.applicationServiceType = applicationServiceType;
        this.exceptionChainId = toExceptionChainId(align.getAnnotationBoList());
    }

    String toSimpleExceptionName(String exceptionClass) {
        if (exceptionClass == null) {
            return "";
        }
        final int index = exceptionClass.lastIndexOf('.');
        if (index != -1) {
            exceptionClass = exceptionClass.substring(index + 1);
        }
        return exceptionClass;
    }

    long toExceptionChainId(List<AnnotationBo> annotationBoList) {
        for (AnnotationBo annotationBo : annotationBoList) {
            if (annotationBo.getKey() == AnnotationKey.EXCEPTION_CHAIN_ID.getCode()
                    && annotationBo.getValue() instanceof Long longValue) {
                return longValue;
            }
        }
        return -1;
    }

    String buildArgument(Align align) {
        final ExceptionInfo exceptionInfo = align.getExceptionInfo();
        if (exceptionInfo == null) {
            return "";
        }
        return Objects.toString(exceptionInfo.message(), "");
    }
}
